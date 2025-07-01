package de.lazycarbs.calculator.core;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;

public class IntermediateFactorCalculator {

    // double leanBeFactor, double pureCarbBeFactor, double beSum, double beCalories

    public IntermediateBolusFactors calculateIntermediateBolusFactors(double mealCarbs, double mealCalories, double usualBolusFactor, double usualBeCalories) {
        double leanBeFactor = ((100 + 100) / (usualBeCalories + 100)) * usualBolusFactor;
        double pureCarbBeFactor = ((50+100) / (usualBeCalories + 100)) * usualBolusFactor;
        double beSum = mealCarbs / 12;
        double beCalories;
        if (beSum == 0) {
            beCalories = 0.0; // Oder Double.NaN, je nach gewünschtem Verhalten
            // Vielleicht auch eine Log-Meldung oder eine spezielle Information an den Benutzer
        } else {
            beCalories = mealCalories / beSum;
        }
        double fatProteinCalories = mealCalories - (beSum * 50);

        return new IntermediateBolusFactors(leanBeFactor, pureCarbBeFactor, beSum, beCalories, fatProteinCalories);
    }
}
