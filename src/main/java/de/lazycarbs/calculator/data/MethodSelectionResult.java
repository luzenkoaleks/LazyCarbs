package de.lazycarbs.calculator.data;

import de.lazycarbs.calculator.methodstrategy.CalculationStrategy;

public record MethodSelectionResult(CalculationStrategy strategy, String explanation) {
}
