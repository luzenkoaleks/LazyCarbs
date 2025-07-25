// START: src/main/java/de/lazycarbs/calculator/controller/CalculatorController.java
package de.lazycarbs.calculator.controller;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.data.MethodSelectionResult;
import de.lazycarbs.calculator.database.DatabaseManager;
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import de.lazycarbs.calculator.data.HourlyBolusFactor;
import de.lazycarbs.calculator.data.CalorieFactors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * REST-Controller für den LazyCarbs Rechner.
 * Stellt Endpunkte für die Berechnung und optionale Datenspeicherung bereit,
 * sowie Endpunkte zur Verwaltung der stündlichen Bolusfaktoren und Kalorienfaktoren.
 */
@RestController
@RequestMapping("/api") // Basis-Pfad für alle Endpunkte in diesem Controller
@CrossOrigin(origins = {"http://localhost:5173", "https://iridescent-semifreddo-01e3ae.netlify.app"}) // CORS-Konfiguration

public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    // Der geheime API-Key, geladen aus application.properties oder Umgebungsvariablen
    @Value("${api.key.secret}")
    private String apiSecretKey;

    private final IntermediateFactorCalculator intermediateFactorCalculator;
    private final MethodCalculationSelector methodCalculationSelector;
    private final FinalBolusCalculator finalBolusCalculator;
    private final BolusFactorCalculator bolusFactorCalculator;
    private final DatabaseManager databaseManager;

    public CalculatorController(IntermediateFactorCalculator intermediateFactorCalculator,
                                MethodCalculationSelector methodCalculationSelector,
                                FinalBolusCalculator finalBolusCalculator,
                                BolusFactorCalculator bolusFactorCalculator,
                                DatabaseManager databaseManager) {
        this.intermediateFactorCalculator = intermediateFactorCalculator;
        this.methodCalculationSelector = methodCalculationSelector;
        this.finalBolusCalculator = finalBolusCalculator;
        this.bolusFactorCalculator = bolusFactorCalculator;
        this.databaseManager = databaseManager;
    }

    /**
     * Verarbeitet POST-Anfragen zur Berechnung des Bolus.
     * Endpunkt: /api/calculate
     * @param requestBody Das Request-Objekt mit allen Eingabedaten, einschließlich der Option zur Datenbankspeicherung.
     * @param httpRequest Das HttpServletRequest-Objekt, um Header auszulesen.
     * @return Ein ResponseEntity mit dem Ergebnis der Berechnung und dem HTTP-Status.
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculateBolus(@RequestBody CalculationRequest requestBody, HttpServletRequest httpRequest) {
        String dbStatusMessage = "Datenbank-Speicherung: Deaktiviert (nicht angefragt)";

        try {
            // Prüfe, ob der API-Key im Backend konfiguriert ist
            if (apiSecretKey == null || apiSecretKey.isEmpty()) {
                logger.error("API Secret Key ist im Backend nicht konfiguriert. Kann API-Key-Validierung nicht durchführen.");
                // Wenn der Server-API-Key nicht konfiguriert ist, kann keine Speicherung erfolgen
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new CalculationResponse(
                                0.0, 0.0, 0.0, 0.0, 0, 0,
                                0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                                0.0, 0.0, 0.0, 0.0, 0.0,
                                0.0, 0.0,
                                "N/A",
                                "N/A",
                                "Fehler: Server-API-Key nicht konfiguriert.",
                                "N/A"
                        ));
            }

            double usualBolusFactor = bolusFactorCalculator.calculateAverageBolusFactor(requestBody.currentHour(), requestBody.currentMinute(), 120);

            IntermediateBolusFactors intermediateBolusFactors = intermediateFactorCalculator.calculateIntermediateBolusFactors(
                    requestBody.mealCarbs(), requestBody.mealCalories(), usualBolusFactor, requestBody.usualBeCalories());

            MethodSelectionResult methodSelection = methodCalculationSelector.selectStrategy(
                    requestBody.mealCarbs(), requestBody.usualBeCalories(), intermediateBolusFactors);

            if (methodSelection.strategy() == null) {
                logger.error("Keine Berechnungsmethode ausgewählt: {}", methodSelection.explanation());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CalculationResponse(
                                0.0, 0.0, 0.0, 0.0, 0, 0,
                                0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                                0.0, 0.0, 0.0, 0.0, 0.0,
                                0.0, 0.0,
                                "N/A",
                                "N/A",
                                "Fehler: " + methodSelection.explanation(),
                                "N/A"
                        ));
            }

            MethodResults methodResults = methodSelection.strategy().calculate(
                    requestBody.mealCarbs(), requestBody.mealCalories(), usualBolusFactor, requestBody.usualBeCalories(),
                    requestBody.insulinTypeCalorieCovering(), intermediateBolusFactors);

            double finalCorrectBolus = finalBolusCalculator.correctBolusSumAdjustment(methodResults, requestBody.movementFactor());

            // NEU: API-Key-Prüfung für die Datenbank-Speicherung mit Rückgabe von 401
            if (requestBody.enableDatabaseStorage()) {
                String requestApiKey = httpRequest.getHeader("X-API-Key"); // Lese den API-Key aus dem Header

                if (requestApiKey == null || !requestApiKey.equals(apiSecretKey)) {
                    logger.warn("Ungültiger oder fehlender API-Key für Datenbank-Speicherung von IP: {}", httpRequest.getRemoteAddr());
                    // WICHTIG: Hier geben wir jetzt einen 401 Unauthorized Status zurück!
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new CalculationResponse(
                                    requestBody.mealCarbs(), requestBody.mealCalories(), requestBody.usualBeCalories(),
                                    requestBody.insulinTypeCalorieCovering(), requestBody.currentHour(), requestBody.currentMinute(),
                                    usualBolusFactor,
                                    intermediateBolusFactors.leanBeFactor(), intermediateBolusFactors.pureCarbBeFactor(),
                                    intermediateBolusFactors.beSum(), intermediateBolusFactors.beCalories(),
                                    intermediateBolusFactors.fatProteinCalories(),
                                    methodResults.correctBeFactor(), methodResults.calorieSurplus(),
                                    methodResults.delayedCalorieBolus(), methodResults.correctBolusSum(),
                                    methodResults.fatProteinCalories(),
                                    requestBody.movementFactor(), finalCorrectBolus,
                                    methodSelection.strategy().getClass().getSimpleName(),
                                    methodSelection.explanation(),
                                    "Berechnung erfolgreich, aber Speicherung fehlgeschlagen.", // Status für Frontend
                                    "Datenbank-Speicherung: Fehlgeschlagen (Ungültiger/Fehlender API-Key)"
                            ));
                } else {
                    try {
                        databaseManager.saveCalculation(
                                requestBody.mealCarbs(), requestBody.mealCalories(), requestBody.usualBeCalories(),
                                requestBody.insulinTypeCalorieCovering(), requestBody.currentHour(), requestBody.currentMinute(),
                                usualBolusFactor, intermediateBolusFactors,
                                methodSelection.strategy().getClass().getSimpleName(), methodSelection.explanation(),
                                methodResults,
                                requestBody.movementFactor(), finalCorrectBolus
                        );
                        logger.info("Berechnungsdaten erfolgreich in der Datenbank gespeichert.");
                        dbStatusMessage = "Datenbank-Speicherung: Erfolgreich";
                    } catch (SQLException e) {
                        logger.error("Fehler beim Speichern der Daten in der Datenbank: {}", e.getMessage(), e);
                        dbStatusMessage = "Datenbank-Speicherung: Fehler (" + e.getMessage() + ")";
                    } catch (Exception e) {
                        logger.error("Fehler bei der Datenbank-Initialisierung (während Speichern): {}", e.getMessage(), e);
                        dbStatusMessage = "Datenbank-Speicherung: Fehler bei Initialisierung (" + e.getMessage() + ")";
                    }
                }
            } else {
                dbStatusMessage = "Datenbank-Speicherung: Deaktiviert (vom Benutzer)";
            }

            CalculationResponse response = new CalculationResponse(
                    requestBody.mealCarbs(), requestBody.mealCalories(), requestBody.usualBeCalories(),
                    requestBody.insulinTypeCalorieCovering(), requestBody.currentHour(), requestBody.currentMinute(),
                    usualBolusFactor,
                    intermediateBolusFactors.leanBeFactor(), intermediateBolusFactors.pureCarbBeFactor(),
                    intermediateBolusFactors.beSum(), intermediateBolusFactors.beCalories(),
                    intermediateBolusFactors.fatProteinCalories(),
                    methodResults.correctBeFactor(), methodResults.calorieSurplus(),
                    methodResults.delayedCalorieBolus(), methodResults.correctBolusSum(),
                    methodResults.fatProteinCalories(),
                    requestBody.movementFactor(), finalCorrectBolus,
                    methodSelection.strategy().getClass().getSimpleName(),
                    methodSelection.explanation(),
                    "Berechnung erfolgreich durchgeführt.",
                    dbStatusMessage
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Fehler bei der Bolus-Berechnung: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CalculationResponse(
                            0.0, 0.0, 0.0, 0.0, 0, 0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0,
                            "N/A",
                            "N/A",
                            "Fehler bei der Berechnung: " + e.getMessage(),
                            dbStatusMessage // Hier den letzten bekannten Status oder "Fehler" setzen
                    ));
        }
    }

    /**
     * NEU: Endpunkt zum Abrufen aller stündlichen Bolusfaktoren.
     * Endpunkt: /api/bolus-factors
     * @return Eine Liste von HourlyBolusFactor-Objekten sortiert nach Stunde.
     */
    @GetMapping("/bolus-factors")
    public ResponseEntity<List<HourlyBolusFactor>> getAllHourlyBolusFactors() {
        try {
            List<HourlyBolusFactor> factors = databaseManager.getAllHourlyBolusFactors();
            return ResponseEntity.ok(factors);
        } catch (SQLException e) {
            logger.error("Fehler beim Abrufen der stündlichen Bolusfaktoren: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * NEU: Endpunkt zum Aktualisieren eines stündlichen Bolusfaktors.
     * Endpunkt: /api/bolus-factors/{hour}
     * @param hour Die Stunde (0-23), die aktualisiert werden soll.
     * @param factor Das HourlyBolusFactor-Objekt mit dem neuen Bolusfaktor.
     * @return Ein ResponseEntity mit einer Statusnachricht.
     */
    @PutMapping("/bolus-factors/{hour}")
    public ResponseEntity<String> updateHourlyBolusFactor(
            @PathVariable int hour,
            @RequestBody HourlyBolusFactor factor) {
        if (hour != factor.hour()) {
            logger.warn("Stunde im Pfad ({}) und im Body ({}) stimmen nicht überein.", hour, factor.hour());
            return ResponseEntity.badRequest().body("Stunde im Pfad und im Body stimmen nicht überein.");
        }
        if (hour < 0 || hour > 23) {
            logger.warn("Ungültige Stunde ({}) für Update-Anfrage.", hour);
            return ResponseEntity.badRequest().body("Stunde muss zwischen 0 und 23 liegen.");
        }
        if (factor.bolusFactor() <= 0) {
            logger.warn("Ungültiger Bolusfaktor ({}) für Stunde {}. Muss positiv sein.", factor.bolusFactor(), hour);
            return ResponseEntity.badRequest().body("Bolusfaktor muss positiv sein.");
        }

        try {
            databaseManager.updateHourlyBolusFactor(hour, factor.bolusFactor());
            logger.info("Bolusfaktor für Stunde {} erfolgreich aktualisiert auf {}.", hour, factor.bolusFactor());
            return ResponseEntity.ok("Bolusfaktor für Stunde " + hour + " erfolgreich aktualisiert.");
        } catch (SQLException e) {
            logger.error("Fehler beim Aktualisieren des Bolusfaktors für Stunde {}: {}", hour, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Aktualisieren des Bolusfaktors.");
        }
    }

    /**
     * NEU: Endpunkt zum Abrufen der Kalorienfaktoren.
     * Endpunkt: /api/calorie-factors
     * @return Ein ResponseEntity mit den CalorieFactors, falls vorhanden, ansonsten NOT_FOUND.
     */
    @GetMapping("/calorie-factors")
    public ResponseEntity<CalorieFactors> getCalorieFactors() {
        try {
            Optional<CalorieFactors> factors = databaseManager.getCalorieFactors();
            if (factors.isPresent()) {
                logger.info("Kalorienfaktoren erfolgreich abgerufen: {}", factors.get());
                return ResponseEntity.ok(factors.get());
            } else {
                logger.warn("Keine Kalorienfaktoren in der Datenbank gefunden.");
                return ResponseEntity.notFound().build();
            }
        } catch (SQLException e) {
            logger.error("Fehler beim Abrufen der Kalorienfaktoren: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * NEU: Endpunkt zum Aktualisieren der Kalorienfaktoren.
     * Endpunkt: /api/calorie-factors
     * @param factors Das CalorieFactors-Objekt mit den neuen Werten.
     * @return Ein ResponseEntity mit einer Statusnachricht.
     */
    @PutMapping("/calorie-factors")
    public ResponseEntity<String> updateCalorieFactors(@RequestBody CalorieFactors factors) {
        if (factors.usualBeCalories() <= 0 || factors.insulinTypeCalorieCovering() <= 0) {
            logger.warn("Ungültige Kalorienfaktoren für Update-Anfrage: usualBeCalories={}, insulinTypeCalorieCovering={}", factors.usualBeCalories(), factors.insulinTypeCalorieCovering());
            return ResponseEntity.badRequest().body("Beide Kalorienfaktoren müssen positive Zahlen sein.");
        }
        try {
            databaseManager.updateCalorieFactors(factors.usualBeCalories(), factors.insulinTypeCalorieCovering());
            // KORREKTUR: Zugriff auf Record-Felder über Accessor-Methoden
            logger.info("Kalorienfaktoren erfolgreich aktualisiert auf: usualBeCalories={}, insulinTypeCalorieCovering={}", factors.usualBeCalories(), factors.insulinTypeCalorieCovering());
            return ResponseEntity.ok("Kalorienfaktoren erfolgreich aktualisiert.");
        } catch (SQLException e) {
            logger.error("Fehler beim Aktualisieren der Kalorienfaktoren: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Aktualisieren der Kalorienfaktoren.");
        }
    }
}
