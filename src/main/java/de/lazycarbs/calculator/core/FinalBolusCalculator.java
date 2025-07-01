package de.lazycarbs.calculator.core;

import de.lazycarbs.calculator.data.MethodResults;

public class FinalBolusCalculator {

    // Berechnet die Höhe des Sofort-Bolus angepasst an den erwarteten Bewegungs-Faktor:

    public double correctBolusSumAdjustment(MethodResults methodResults, double movementFactor) {
        double adjustedCorrectBolusSum = methodResults.correctBolusSum() * movementFactor;
        return adjustedCorrectBolusSum;
    }
}
