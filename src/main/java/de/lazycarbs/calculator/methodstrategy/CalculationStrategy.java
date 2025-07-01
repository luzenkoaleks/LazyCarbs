package de.lazycarbs.calculator.methodstrategy;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;

public interface CalculationStrategy {

    // double mealCarbs, double mealCalories, double usualBolusFactor, double usualBeCalories, IntermediateBolusFactors
    MethodResults calculate(double mealCarbs, double mealCalories, double usualBolusFactor,
                            double usualBeCalories, double insulinTypeCalorieCovering, IntermediateBolusFactors intermediateBolusFactors);
}
