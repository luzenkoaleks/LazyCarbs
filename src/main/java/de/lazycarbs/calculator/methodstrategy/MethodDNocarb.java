package de.lazycarbs.calculator.methodstrategy;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;

public class MethodDNocarb implements CalculationStrategy{
    @Override
    public MethodResults calculate(double mealCarbs, double mealCalories, double usualBolusFactor,
                                   double usualBeCalories, double insulinTypeCalorieCovering, IntermediateBolusFactors intermediateBolusFactors) {
        // we calculate the values of:
        // double correctBeFactor, double calorieSurplus, double delayedCalorieBolus, double correctBolusSum
        double correctBeFactor = 0.0;
        double calorieSurplus = 0.0;
        double delayedCalorieBolus = (mealCalories / 200) * intermediateBolusFactors.leanBeFactor(); // delayed over 8 hours
        double correctBolusSum = 0.0;
        double fatProteinCalories = intermediateBolusFactors.fatProteinCalories();


        return new MethodResults(correctBeFactor, calorieSurplus, delayedCalorieBolus, correctBolusSum, fatProteinCalories);
    }
}
