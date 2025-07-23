package de.lazycarbs.calculator.config;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.HourlyBolusFactor;
import de.lazycarbs.calculator.database.DatabaseManager;
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger; // NEU: Import für Logger
import org.slf4j.LoggerFactory; // NEU: Import für LoggerFactory

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring-Konfigurationsklasse zum Definieren von Beans.
 * Stellt sicher, dass die Kernkomponenten des Rechners als Singletons
 * im Spring-Kontext verfügbar sind und initialisiert die Datenbank bei Bedarf.
 */
@Configuration
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class); // NEU: Logger Instanz

    // Standardwerte für die stündlichen Bolusfaktoren,
    // die verwendet werden, wenn die Datenbanktabelle leer ist.
    private static final double[] DEFAULT_HOURLY_FACTORS = {
            0.83, 0.77, 0.72, 0.72, 0.77, 0.88, // 0-5 Uhr
            0.99, 1.14, 1.10, 1.27, 1.48, 1.25, // 6-11 Uhr
            1.02, 0.81, 0.81, 0.81, 0.81, 0.81, // 12-17 Uhr
            0.81, 1.01, 1.01, 1.01, 1.01, 1.01  // 18-23 Uhr
    };

    // NEU: Standardwerte für Kalorienfaktoren
    private static final double DEFAULT_USUAL_BE_CALORIES = 105.0;
    private static final double DEFAULT_INSULIN_TYPE_CALORIE_COVERING = 200.0;

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

    // DatabaseManager als Spring Bean definieren
    @Bean
    public DatabaseManager databaseManager() {
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            logger.warn("Datenbankpasswort nicht als Umgebungsvariable 'DB_PASSWORD' gesetzt. Datenbank-Zugriff für DatabaseManager eingeschränkt.");
            // Gibt einen DatabaseManager zurück, der aber keine Verbindung aufbauen kann,
            // wenn das Passwort fehlt. Die Methoden im DatabaseManager selbst prüfen dies.
            return new DatabaseManager("jdbc:mysql://localhost:3306/lazycarbs_db", "lazyuser", ""); // Leeres Passwort, wird fehlschlagen
        }
        return new DatabaseManager(
                "jdbc:mysql://localhost:3306/lazycarbs_db",
                "lazyuser",
                dbPassword
        );
    }

    // BolusFactorCalculator als Spring Bean definieren und DatabaseManager injizieren
    @Bean
    public BolusFactorCalculator bolusFactorCalculator(DatabaseManager databaseManager) {
        return new BolusFactorCalculator(databaseManager);
    }

    /**
     * Event Listener, der auf das Ende der Spring-Anwendungsinitialisierung wartet.
     * Wird verwendet, um die 'hourly_bolus_factors'-Tabelle und die 'calorie_factors'-Tabelle
     * mit Standardwerten zu initialisieren, falls sie leer sind.
     * @param event Das ContextRefreshedEvent.
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        DatabaseManager dbManager = event.getApplicationContext().getBean(DatabaseManager.class);
        try {
            // Initialisiere stündliche Bolusfaktoren
            List<HourlyBolusFactor> defaultFactorsList = new ArrayList<>();
            for (int i = 0; i < DEFAULT_HOURLY_FACTORS.length; i++) {
                defaultFactorsList.add(new HourlyBolusFactor(i, DEFAULT_HOURLY_FACTORS[i]));
            }
            dbManager.initializeHourlyBolusFactors(defaultFactorsList);

            // NEU: Initialisiere Kalorienfaktoren
            dbManager.initializeCalorieFactors(DEFAULT_USUAL_BE_CALORIES, DEFAULT_INSULIN_TYPE_CALORIE_COVERING);

        } catch (SQLException e) {
            logger.error("FEHLER beim Initialisieren der Datenbanktabellen (Bolusfaktoren oder Kalorienfaktoren): {}", e.getMessage(), e);
        }
    }
}
