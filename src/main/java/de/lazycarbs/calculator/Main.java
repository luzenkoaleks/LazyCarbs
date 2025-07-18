package de.lazycarbs.calculator;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.data.MethodSelectionResult;
import de.lazycarbs.calculator.database.DatabaseManager;
import de.lazycarbs.calculator.input.InputHandler;
import de.lazycarbs.calculator.methodstrategy.CalculationStrategy;
import de.lazycarbs.calculator.output.OutputHandler;
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        InputHandler inputHandler = new InputHandler();
        OutputHandler outputHandler = new OutputHandler();
        IntermediateFactorCalculator intermediateFactorCalculator = new IntermediateFactorCalculator();
        MethodCalculationSelector methodCalculationSelector = new MethodCalculationSelector();
        FinalBolusCalculator finalBolusCalculator = new FinalBolusCalculator();
        BolusFactorCalculator bolusFactorCalculator = new BolusFactorCalculator();

        // --- Start: Logik für optionales Datenbank-Speichern ---
        boolean enableDatabaseStorage = true; // Standardmäßig ist die Speicherung aktiviert
        for (String arg : args) {
            if ("--no-db".equalsIgnoreCase(arg)) {
                enableDatabaseStorage = false;
                break;
            }
        }

        DatabaseManager databaseManager = null; // Initialisiere als null
        boolean databaseManagerInitialized = false; // Flag, um zu verfolgen, ob DatabaseManager erfolgreich erstellt wurde

        if (enableDatabaseStorage) {
            // Das Passwort wird aus einer Umgebungsvariable gelesen (Run/Debug Configurations)
            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword == null || dbPassword.isEmpty()) {
                outputHandler.displayMessage("FEHLER: Das Datenbankpasswort ist nicht als Umgebungsvariable 'DB_PASSWORD' gesetzt.");
                outputHandler.displayMessage("Bitte setze die Umgebungsvariable, z.B. 'export DB_PASSWORD=\"dein_passwort\"' im Terminal.");
                outputHandler.displayMessage("Das Programm wird ohne Datenbank-Speicherung fortgesetzt.");
            } else {
                try {
                    databaseManager = new DatabaseManager(
                            "jdbc:mysql://localhost:3306/lazycarbs_db",
                            "lazyuser",
                            dbPassword
                    );
                    databaseManagerInitialized = true; // Flag setzen, wenn Initialisierung erfolgreich war
                } catch (Exception e) { // Fange jede Exception während der DatabaseManager-Erstellung ab
                    outputHandler.displayMessage("FEHLER: Konnte DatabaseManager nicht initialisieren: " + e.getMessage());
                    System.err.println("Detaillierter Fehler bei DatabaseManager-Initialisierung: " + e.getMessage());
                    outputHandler.displayMessage("Das Programm wird ohne Datenbank-Speicherung fortgesetzt.");
                }
            }
        } else {
                outputHandler.displayMessage("\nDas Programm wird ohne Datenbank-Speicherung ausgeführt (--no-db Option).");
            }
        // --- Ende: Logik für optionales Datenbank-Speichern und Passwort-Prüfung ---

        outputHandler.displayLazyCarbs();

        // User Eingabe der Werte für:
        // double mealCarbs, double mealCalories, double usualBolusFactor, double usualBeCalories
        double mealCarbs = inputHandler.readDouble("Enter the Carbs of your meal: ");
        double mealCalories = inputHandler.readDouble("Enter the Calories of your meal: ");
        // double usualBolusFactor = inputHandler.readDouble("Enter the BE-Bolus-factor for this hour: "); ** entfernt um den präziseren durchschnittlichen Bolus-faktor über Zeitangabe zu nutzen
        double usualBeCalories = inputHandler.readDouble("Enter your general average Calories for a BE: ");
        double insulinTypeCalorieCovering = inputHandler.readDouble("Gib ein 150(für Analog-Insulin) oder 200(für Normalinsulin) (150 / 200): ");
        // NEU: Uhrzeit für Bolusfaktor-Berechnung eingeben
        outputHandler.displayMessage("\nUm wie viel Uhr möchtest du essen? (z.B. 14:38 = Stunde 14 : Minute 38)");
        int currentHour = inputHandler.readInt("Welche Stunde (0-23): ");
        int currentMinute = inputHandler.readInt("Welche Minute (0-59): ");

        // NEU: usualBolusFactor basierend auf Uhrzeit berechnen
        // Berechnung des durchschnittlichen Faktors für einen 120-Minuten-Zeitraum
        double usualBolusFactor = bolusFactorCalculator.calculateAverageBolusFactor(currentHour, currentMinute, 120);

        IntermediateBolusFactors intermediateBolusFactors = intermediateFactorCalculator.calculateIntermediateBolusFactors(mealCarbs, mealCalories, usualBolusFactor, usualBeCalories);

        outputHandler.displayUserEntry("\n*** Deine Eingabe: ***", mealCarbs, mealCalories, usualBeCalories, insulinTypeCalorieCovering,
                currentHour, currentMinute, usualBolusFactor, intermediateBolusFactors);

        MethodSelectionResult methodSelection = methodCalculationSelector.selectStrategy(mealCarbs,usualBeCalories, intermediateBolusFactors);
        CalculationStrategy selectedStrategy = methodSelection.strategy();

        outputHandler.displayMessage(methodSelection.explanation());

        MethodResults methodResults = selectedStrategy.calculate(mealCarbs, mealCalories,usualBolusFactor, usualBeCalories, insulinTypeCalorieCovering, intermediateBolusFactors);

        outputHandler.displayCalculationResults("*** Relevante Größen für deine Mahlzeit: ***", intermediateBolusFactors,methodResults, selectedStrategy, mealCalories, usualBeCalories, usualBolusFactor, mealCarbs);

        double movementFactor = inputHandler.readDouble("\nGib den Bewegungs-Faktor für diese Mahlzeit ein: ");

        double finalCorrectBolus = finalBolusCalculator.correctBolusSumAdjustment(methodResults, movementFactor);

        outputHandler.displayResult("Korrekter Sofort-Bolus (angepasst an Bewegungs-Faktor): ", finalCorrectBolus);



        // Daten in der Datenbank speichern - NUR wenn databaseManager erfolgreich initialisiert wurde
        if (databaseManagerInitialized) { // Verwende das Flag, um zu prüfen, ob die DB zur Verwendung bereit ist
        try {
            databaseManager.saveCalculation(
                    mealCarbs, mealCalories, usualBeCalories, insulinTypeCalorieCovering,
                    currentHour, currentMinute, usualBolusFactor,
                    intermediateBolusFactors,
                    selectedStrategy.getClass().getSimpleName(), methodSelection.explanation(),
                    methodResults,
                    movementFactor, finalCorrectBolus
            );
            outputHandler.displayMessage("\nBerechnungsdaten erfolgreich in der Datenbank gespeichert.");
        } catch (SQLException e) {
            outputHandler.displayMessage("\nFehler beim Speichern der Daten in der Datenbank: " + e.getMessage());
            System.err.println("SQL-Fehler: " + e.getMessage()); // Für detaillierte Fehlermeldung in der Konsole

        } finally {
            // Sicherstellen, dass die Datenbankverbindung geschlossen wird
            try {
                if (databaseManager != null) { // Prüfen, ob databaseManager initialisiert wurde
                    databaseManager.closeConnection();
                }
            } catch (SQLException e) {
                outputHandler.displayMessage("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
                System.err.println("Detaillierter Fehler beim Schließen der Verbindung: " + e.getMessage());
            }
        }
        }

        outputHandler.displayMessage("\nBerechnung abgeschlossen. Vielen Dank!");

    }
}
