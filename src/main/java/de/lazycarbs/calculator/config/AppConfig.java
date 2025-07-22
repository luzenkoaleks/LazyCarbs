package de.lazycarbs.calculator.config;

import de.lazycarbs.calculator.core.FinalBolusCalculator;
import de.lazycarbs.calculator.core.IntermediateFactorCalculator;
import de.lazycarbs.calculator.core.MethodCalculationSelector;
import de.lazycarbs.calculator.data.HourlyBolusFactor; // NEU: Import für HourlyBolusFactor
import de.lazycarbs.calculator.database.DatabaseManager; // NEU: Import für DatabaseManager
import de.lazycarbs.calculator.util.BolusFactorCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent; // NEU: Import für Event Listener
import org.springframework.context.event.EventListener; // NEU: Import für Event Listener

import java.sql.SQLException;
import java.util.ArrayList; // NEU: Import für ArrayList
import java.util.List;      // NEU: Import für List

/**
 * Spring-Konfigurationsklasse zum Definieren von Beans.
 * Stellt sicher, dass die Kernkomponenten des Rechners als Singletons
 * im Spring-Kontext verfügbar sind und initialisiert die Datenbank bei Bedarf.
 */
@Configuration
public class AppConfig {

    // Standardwerte für die stündlichen Bolusfaktoren,
    // die verwendet werden, wenn die Datenbanktabelle leer ist.
    private static final double[] DEFAULT_HOURLY_FACTORS = {
            0.83, 0.77, 0.72, 0.72, 0.77, 0.88, // 0-5 Uhr
            0.99, 1.14, 1.10, 1.27, 1.48, 1.25, // 6-11 Uhr
            1.02, 0.81, 0.81, 0.81, 0.81, 0.81, // 12-17 Uhr
            0.81, 1.01, 1.01, 1.01, 1.01, 1.01  // 18-23 Uhr
    };

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

    // NEU: DatabaseManager als Spring Bean definieren
    @Bean
    public DatabaseManager databaseManager() {
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("WARNUNG: Datenbankpasswort nicht als Umgebungsvariable 'DB_PASSWORD' gesetzt. Datenbank-Zugriff für DatabaseManager eingeschränkt.");
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

    // NEU: BolusFactorCalculator als Spring Bean definieren und DatabaseManager injizieren
    @Bean
    public BolusFactorCalculator bolusFactorCalculator(DatabaseManager databaseManager) {
        return new BolusFactorCalculator(databaseManager);
    }

    /**
     * Event Listener, der auf das Ende der Spring-Anwendungsinitialisierung wartet.
     * Wird verwendet, um die 'hourly_bolus_factors'-Tabelle mit Standardwerten zu initialisieren,
     * falls sie leer ist.
     * @param event Das ContextRefreshedEvent.
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        DatabaseManager dbManager = event.getApplicationContext().getBean(DatabaseManager.class);
        try {
            List<HourlyBolusFactor> defaultFactorsList = new ArrayList<>();
            for (int i = 0; i < DEFAULT_HOURLY_FACTORS.length; i++) {
                defaultFactorsList.add(new HourlyBolusFactor(i, DEFAULT_HOURLY_FACTORS[i]));
            }
            dbManager.initializeHourlyBolusFactors(defaultFactorsList);
        } catch (SQLException e) {
            System.err.println("FEHLER beim Initialisieren der stündlichen Bolusfaktoren in der Datenbank: " + e.getMessage());
        }
    }
}
