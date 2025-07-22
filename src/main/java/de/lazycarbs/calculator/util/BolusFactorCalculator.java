package de.lazycarbs.calculator.util;

import de.lazycarbs.calculator.data.HourlyBolusFactor;
import de.lazycarbs.calculator.database.DatabaseManager; // NEU: Import für DatabaseManager
import org.springframework.stereotype.Component; // NEU: Import für @Component

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // NEU: Import für Collectors

/**
 * Berechnet den durchschnittlichen Bolusfaktor basierend auf der aktuellen Uhrzeit
 * und den in der Datenbank gespeicherten stündlichen Faktoren.
 */
@Component // NEU: Markiert diese Klasse als Spring Component, damit sie injiziert werden kann
public class BolusFactorCalculator {

    // Die stündlichen Bolusfaktoren werden nicht mehr hartkodiert,
    // sondern aus der Datenbank geladen.
    private final DatabaseManager databaseManager; // NEU: Abhängigkeit von DatabaseManager

    // NEU: Konstruktor für Dependency Injection
    public BolusFactorCalculator(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        // Beim Start der Anwendung könnten hier die Standardfaktoren initialisiert werden,
        // falls die Tabelle leer ist. Dies wird im AppConfig erfolgen, um die Logik zu trennen.
    }

    /**
     * Berechnet den durchschnittlichen Bolusfaktor für einen bestimmten Zeitraum
     * basierend auf den in der Datenbank gespeicherten stündlichen Bolusfaktoren.
     *
     * @param currentHour Die aktuelle Stunde (0-23).
     * @param currentMinute Die aktuelle Minute (0-59).
     * @param durationMinutes Die Dauer des Zeitraums in Minuten, für den der Durchschnitt berechnet werden soll.
     * @return Der durchschnittliche Bolusfaktor für den angegebenen Zeitraum.
     */
    public double calculateAverageBolusFactor(int currentHour, int currentMinute, int durationMinutes) {
        Map<Integer, Double> hourlyFactors = loadHourlyBolusFactorsFromDb(); // Lade Faktoren aus DB

        if (hourlyFactors.isEmpty()) {
            System.err.println("WARNUNG: Keine stündlichen Bolusfaktoren aus der Datenbank geladen. Verwende Fallback-Standardwerte.");
            // Fallback-Werte, falls die Datenbank leer ist oder nicht erreicht werden kann.
            // Diese sollten idealerweise mit den Werten übereinstimmen, die du in die DB eingefügt hast.
            return getFallbackBolusFactor(currentHour); // Nutze eine Fallback-Methode
        }

        double totalFactor = 0;
        int count = 0;

        for (int i = 0; i < durationMinutes; i++) {
            int hour = (currentHour + (currentMinute + i) / 60) % 24;
            if (hourlyFactors.containsKey(hour)) {
                totalFactor += hourlyFactors.get(hour);
                count++;
            } else {
                // Falls für eine Stunde kein Eintrag in der DB ist, nutze den Fallback-Wert
                totalFactor += getFallbackBolusFactor(hour);
                count++;
            }
        }

        return count > 0 ? totalFactor / count : getFallbackBolusFactor(currentHour);
    }

    /**
     * Lädt alle stündlichen Bolusfaktoren aus der Datenbank.
     * @return Eine Map von Stunde zu Bolusfaktor.
     */
    private Map<Integer, Double> loadHourlyBolusFactorsFromDb() {
        try {
            List<HourlyBolusFactor> factorsList = databaseManager.getAllHourlyBolusFactors();
            return factorsList.stream()
                    .collect(Collectors.toMap(HourlyBolusFactor::hour, HourlyBolusFactor::bolusFactor));
        } catch (SQLException e) {
            System.err.println("FEHLER: Konnte stündliche Bolusfaktoren nicht aus der Datenbank laden: " + e.getMessage());
            // Hier könnte man auch eine leere Map zurückgeben und die aufrufende Methode den Fallback nutzen lassen
            return new HashMap<>(); // Gib eine leere Map zurück, um den Fallback zu triggern
        }
    }

    /**
     * Bietet Fallback-Bolusfaktoren, wenn die Datenbank nicht verfügbar ist oder keine Daten enthält.
     * Diese Werte sollten mit den initialen INSERT-Werten in der DB übereinstimmen.
     * @param hour Die Stunde.
     * @return Der Standard-Bolusfaktor für die gegebene Stunde.
     */
    private double getFallbackBolusFactor(int hour) {
        // Diese Werte sollten den initialen Werten in deiner DB entsprechen
        double[] defaultFactors = {
                0.83, 0.77, 0.72, 0.72, 0.77, 0.88, // 0-5 Uhr
                0.99, 1.14, 1.10, 1.27, 1.48, 1.25, // 6-11 Uhr
                1.02, 0.81, 0.81, 0.81, 0.81, 0.81, // 12-17 Uhr
                0.81, 1.01, 1.01, 1.01, 1.01, 1.01  // 18-23 Uhr
        };
        return defaultFactors[hour % 24]; // Sicherstellen, dass die Stunde im Bereich liegt
    }
}
