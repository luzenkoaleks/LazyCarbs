package de.lazycarbs.calculator.controller;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.data.MethodSelectionResult;
import de.lazycarbs.calculator.database.DatabaseManager;
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * REST-Controller für den LazyCarbs Rechner.
 * Stellt Endpunkte für die Berechnung und optionale Datenspeicherung bereit.
 */
@RestController
@RequestMapping("/api/calculate")
@CrossOrigin(origins = "http://localhost:5173") // CORS-Konfiguration
public class CalculatorController {

    private final IntermediateFactorCalculator intermediateFactorCalculator;
    private final MethodCalculationSelector methodCalculationSelector;
    private final FinalBolusCalculator finalBolusCalculator;
    private final BolusFactorCalculator bolusFactorCalculator;

    // Der DatabaseManager wird nicht mehr im Konstruktor initialisiert,
    // sondern bedingt in der calculateBolus-Methode, basierend auf der Anfrage.
    // Die Umgebungsvariable DB_PASSWORD wird bei Bedarf direkt dort geprüft.

    public CalculatorController(IntermediateFactorCalculator intermediateFactorCalculator,
                                MethodCalculationSelector methodCalculationSelector,
                                FinalBolusCalculator finalBolusCalculator,
                                BolusFactorCalculator bolusFactorCalculator) {
        this.intermediateFactorCalculator = intermediateFactorCalculator;
        this.methodCalculationSelector = methodCalculationSelector;
        this.finalBolusCalculator = finalBolusCalculator;
        this.bolusFactorCalculator = bolusFactorCalculator;
    }

    /**
     * Verarbeitet POST-Anfragen zur Berechnung des Bolus.
     * @param request Das Request-Objekt mit allen Eingabedaten, einschließlich der Option zur Datenbankspeicherung.
     * @return Ein ResponseEntity mit dem Ergebnis der Berechnung und dem HTTP-Status.
     */
    @PostMapping
    public ResponseEntity<CalculationResponse> calculateBolus(@RequestBody CalculationRequest request) {
        String dbStatusMessage = "Datenbank-Speicherung: Deaktiviert (nicht angefragt)"; // Standardstatus

        try {
            // Berechnungen basierend auf der bestehenden Logik
            double usualBolusFactor = bolusFactorCalculator.calculateAverageBolusFactor(request.currentHour(), request.currentMinute(), 120);

            IntermediateBolusFactors intermediateBolusFactors = intermediateFactorCalculator.calculateIntermediateBolusFactors(
                    request.mealCarbs(), request.mealCalories(), usualBolusFactor, request.usualBeCalories());

            MethodSelectionResult methodSelection = methodCalculationSelector.selectStrategy(
                    request.mealCarbs(), request.usualBeCalories(), intermediateBolusFactors);

            // Sicherstellen, dass eine Strategie ausgewählt wurde
            if (methodSelection.strategy() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CalculationResponse(
                                0.0, 0.0, 0.0, 0.0, 0, 0, // mealCarbs, mealCalories, usualBeCalories, insulinTypeCalorieCovering, currentHour, currentMinute
                                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // usualBolusFactor, intermediateLeanBeFactor, pureCarbBeFactor, beSum, beCalories, fatProteinCalories
                                0.0, 0.0, 0.0, 0.0, 0.0, // methodCorrectBeFactor, calorieSurplus, delayedCalorieBolus, correctBolusSum, fatProteinCalories
                                0.0, 0.0, // movementFactor, finalCorrectBolus
                                "N/A", // selectedMethodName
                                "N/A", // methodExplanation
                                "Fehler: " + methodSelection.explanation(), // statusMessage
                                "N/A" // dbStatus
                        ));
            }

            MethodResults methodResults = methodSelection.strategy().calculate(
                    request.mealCarbs(), request.mealCalories(), usualBolusFactor, request.usualBeCalories(),
                    request.insulinTypeCalorieCovering(), intermediateBolusFactors);

            double finalCorrectBolus = finalBolusCalculator.correctBolusSumAdjustment(methodResults, request.movementFactor());

            // NEU: Daten in der Datenbank speichern, WENN vom Frontend angefordert UND DB initialisierbar ist
            if (request.enableDatabaseStorage()) { // Prüfe den neuen Request-Parameter
                String dbPassword = System.getenv("DB_PASSWORD");
                if (dbPassword == null || dbPassword.isEmpty()) {
                    System.err.println("WARNUNG: Datenbankpasswort nicht als Umgebungsvariable 'DB_PASSWORD' gesetzt. Datenbank-Speicherung konnte nicht durchgeführt werden.");
                    dbStatusMessage = "Datenbank-Speicherung: Deaktiviert (Passwort fehlt)";
                } else {
                    DatabaseManager databaseManager = null; // Lokale Instanz
                    try {
                        databaseManager = new DatabaseManager(
                                "jdbc:mysql://localhost:3306/lazycarbs_db",
                                "lazyuser",
                                dbPassword
                        );
                        databaseManager.saveCalculation(
                                request.mealCarbs(), request.mealCalories(), request.usualBeCalories(),
                                request.insulinTypeCalorieCovering(), request.currentHour(), request.currentMinute(),
                                usualBolusFactor, intermediateBolusFactors,
                                methodSelection.strategy().getClass().getSimpleName(), methodSelection.explanation(),
                                methodResults,
                                request.movementFactor(), finalCorrectBolus
                        );
                        System.out.println("Berechnungsdaten erfolgreich in der Datenbank gespeichert.");
                        dbStatusMessage = "Datenbank-Speicherung: Erfolgreich";
                    } catch (SQLException e) {
                        System.err.println("Fehler beim Speichern der Daten in der Datenbank: " + e.getMessage());
                        dbStatusMessage = "Datenbank-Speicherung: Fehler (" + e.getMessage() + ")";
                    } catch (Exception e) { // Auch andere Initialisierungsfehler abfangen
                        System.err.println("Fehler bei der Datenbank-Initialisierung: " + e.getMessage());
                        dbStatusMessage = "Datenbank-Speicherung: Fehler bei Initialisierung (" + e.getMessage() + ")";
                    } finally {
                        try {
                            if (databaseManager != null) {
                                databaseManager.closeConnection(); // Verbindung nach jeder Speicherung schließen
                            }
                        } catch (SQLException e) {
                            System.err.println("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
                        }
                    }
                }
            } else {
                dbStatusMessage = "Datenbank-Speicherung: Deaktiviert (vom Benutzer)";
            }

            // Erstelle die Antwort mit allen relevanten Werten
            CalculationResponse response = new CalculationResponse(
                    request.mealCarbs(), request.mealCalories(), request.usualBeCalories(),
                    request.insulinTypeCalorieCovering(), request.currentHour(), request.currentMinute(),
                    usualBolusFactor,
                    intermediateBolusFactors.leanBeFactor(), intermediateBolusFactors.pureCarbBeFactor(),
                    intermediateBolusFactors.beSum(), intermediateBolusFactors.beCalories(),
                    intermediateBolusFactors.fatProteinCalories(),
                    methodResults.correctBeFactor(), methodResults.calorieSurplus(),
                    methodResults.delayedCalorieBolus(), methodResults.correctBolusSum(),
                    methodResults.fatProteinCalories(),
                    request.movementFactor(), finalCorrectBolus,
                    methodSelection.strategy().getClass().getSimpleName(),
                    methodSelection.explanation(),
                    "Berechnung erfolgreich durchgeführt.",
                    dbStatusMessage // Verwende die aktualisierte Statusnachricht
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Fehler bei der Bolus-Berechnung: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CalculationResponse(
                            0.0, 0.0, 0.0, 0.0, 0, 0, // mealCarbs, mealCalories, usualBeCalories, insulinTypeCalorieCovering, currentHour, currentMinute
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // usualBolusFactor, intermediateLeanBeFactor, pureCarbBeFactor, beSum, beCalories, fatProteinCalories
                            0.0, 0.0, 0.0, 0.0, 0.0, // methodCorrectBeFactor, calorieSurplus, delayedCalorieBolus, correctBolusSum, fatProteinCalories
                            0.0, 0.0, // movementFactor, finalCorrectBolus
                            "N/A", // selectedMethodName
                            "N/A", // methodExplanation
                            "Fehler bei der Berechnung: " + e.getMessage(), // statusMessage
                            dbStatusMessage // Auch hier den dbStatusMessage verwenden
                    ));
        }
    }
}
