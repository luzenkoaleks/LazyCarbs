package de.lazycarbs.calculator.config;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-Konfigurationsklasse zum Definieren von Beans.
 * Stellt sicher, dass die Kernkomponenten des Rechners als Singletons
 * im Spring-Kontext verfügbar sind.
 */
@Configuration
public class AppConfig {

    @Bean
    public IntermediateFactorCalculator intermediateFactorCalculator() {
        return new IntermediateFactorCalculator();
    }

    @Bean
    public MethodCalculationSelector methodCalculationSelector() {
        return new MethodCalculationSelector();
    }

    @Bean
    public FinalBolusCalculator finalBolusCalculator() {
        return new FinalBolusCalculator();
    }

    @Bean
    public BolusFactorCalculator bolusFactorCalculator() {
        return new BolusFactorCalculator();
    }
}
