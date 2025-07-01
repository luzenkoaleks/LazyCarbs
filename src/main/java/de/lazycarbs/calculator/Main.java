package de.lazycarbs.calculator;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.data.MethodSelectionResult;
import de.lazycarbs.calculator.input.InputHandler;
import de.lazycarbs.calculator.methodstrategy.CalculationStrategy;
import de.lazycarbs.calculator.output.OutputHandler;
import de.lazycarbs.calculator.util.BolusFactorCalculator;

public class Main {
    public static void main(String[] args) {
        InputHandler inputHandler = new InputHandler();
        OutputHandler outputHandler = new OutputHandler();
        IntermediateFactorCalculator intermediateFactorCalculator = new IntermediateFactorCalculator();
        MethodCalculationSelector methodCalculationSelector = new MethodCalculationSelector();
        FinalBolusCalculator finalBolusCalculator = new FinalBolusCalculator();
        BolusFactorCalculator bolusFactorCalculator = new BolusFactorCalculator();

        outputHandler.displayLazyCarbs();

        // User eingabe der Werte für:
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

    }
}
