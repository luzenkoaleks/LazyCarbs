package de.lazycarbs.calculator.methodstrategy;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;

public class MethodBSupersize implements CalculationStrategy{

    @Override
    public MethodResults calculate(double mealCarbs, double mealCalories, double usualBolusFactor,
                                   double usualBeCalories, double insulinTypeCalorieCovering, IntermediateBolusFactors intermediateBolusFactors) {
        // we calculate the values of:
        // double correctBeFactor, double calorieSurplus, double delayedCalorieBolus, double correctBolusSum
        double correctBeFactor = 0.0;
        double calorieSurplus = 0.0;
        double delayedCalorieBolus = 0.0; // delayed over 8 hours
        double correctBolusSum = 0.0;
        double fatProteinCalories = intermediateBolusFactors.fatProteinCalories();

        if(intermediateBolusFactors.beCalories() <= insulinTypeCalorieCovering) { // double insulinTypeCalorieCovering = 150 || 200
            correctBeFactor = ((intermediateBolusFactors.beCalories() + 100) / (usualBeCalories + 100)) * usualBolusFactor;
            double overhangingBe = intermediateBolusFactors.beSum() - 7.5;
            calorieSurplus = (mealCalories - (intermediateBolusFactors.beCalories() * 7.5)) - (overhangingBe * 50);
            correctBolusSum = (correctBeFactor * 7.5) + (overhangingBe * intermediateBolusFactors.pureCarbBeFactor());
            delayedCalorieBolus = (calorieSurplus / 200) * intermediateBolusFactors.leanBeFactor();

        }
        else if(intermediateBolusFactors.beCalories() > insulinTypeCalorieCovering) {
            correctBeFactor = ((insulinTypeCalorieCovering + 100) / (usualBeCalories + 100)) * usualBolusFactor;
            double overhangingBe = intermediateBolusFactors.beSum() - 7.5;
            calorieSurplus = (mealCalories - (insulinTypeCalorieCovering * 7.5)) - (overhangingBe * 50);
            correctBolusSum = (correctBeFactor * 7.5) + (overhangingBe * intermediateBolusFactors.pureCarbBeFactor());
            delayedCalorieBolus = (calorieSurplus / 200) * intermediateBolusFactors.leanBeFactor();
        }
        return new MethodResults(correctBeFactor, calorieSurplus, delayedCalorieBolus, correctBolusSum, fatProteinCalories);
    }
}
