
package de.lazycarbs.calculator.database;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.data.HourlyBolusFactor;
import org.slf4j.Logger; // Import für Logger
import org.slf4j.LoggerFactory; // Import für LoggerFactory

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet die Datenbankverbindung und Operationen für den LazyCarbs Rechner.
 * Speichert Berechnungsdaten und verwaltet stündliche Bolusfaktoren.
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class); // Logger Instanz

    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;
    private Connection connection;

    public DatabaseManager(String dbUrl, String dbUser, String dbPassword) {
        this.DB_URL = dbUrl;
        this.DB_USER = dbUser;
        this.DB_PASSWORD = dbPassword;
    }

    private void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.debug("Öffne Datenbankverbindung zu {}", DB_URL); // Logging
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            logger.debug("Schließe Datenbankverbindung."); // Logging
            connection.close();
        }
    }

    public void saveCalculation(
            double mealCarbs, double mealCalories, double usualBeCalories,
            double insulinTypeCalorieCovering, int currentHour, int currentMinute,
            double usualBolusFactor, IntermediateBolusFactors intermediateBolusFactors,
            String selectedMethodName, String methodExplanation,
            MethodResults methodResults,
            double movementFactor, double finalCorrectBolus) throws SQLException {

        openConnection();
        String sql = "INSERT INTO calculations (" +
                "timestamp, meal_carbs, meal_calories, usual_be_calories, insulin_type_calorie_covering, " +
                "current_hour, current_minute, usual_bolus_factor, " +
                "intermediate_lean_be_factor, intermediate_pure_carb_be_factor, intermediate_be_sum, " +
                "intermediate_be_calories, intermediate_fat_protein_calories, " +
                "selected_method_name, method_explanation, " +
                "method_correct_be_factor, method_calorie_surplus, method_delayed_calorie_bolus, " +
                "method_correct_bolus_sum, method_fat_protein_calories, " +
                "movement_factor, final_correct_bolus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int i = 1;
            pstmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setDouble(i++, mealCarbs);
            pstmt.setDouble(i++, mealCalories);
            pstmt.setDouble(i++, usualBeCalories);
            pstmt.setDouble(i++, insulinTypeCalorieCovering);
            pstmt.setInt(i++, currentHour);
            pstmt.setInt(i++, currentMinute);
            pstmt.setDouble(i++, usualBolusFactor);
            pstmt.setDouble(i++, intermediateBolusFactors.leanBeFactor());
            pstmt.setDouble(i++, intermediateBolusFactors.pureCarbBeFactor());
            pstmt.setDouble(i++, intermediateBolusFactors.beSum());
            pstmt.setDouble(i++, intermediateBolusFactors.beCalories());
            pstmt.setDouble(i++, intermediateBolusFactors.fatProteinCalories());
            pstmt.setString(i++, selectedMethodName);
            pstmt.setString(i++, methodExplanation);
            pstmt.setDouble(i++, methodResults.correctBeFactor());
            pstmt.setDouble(i++, methodResults.calorieSurplus());
            pstmt.setDouble(i++, methodResults.delayedCalorieBolus());
            pstmt.setDouble(i++, methodResults.correctBolusSum());
            pstmt.setDouble(i++, methodResults.fatProteinCalories());
            pstmt.setDouble(i++, movementFactor);
            pstmt.setDouble(i++, finalCorrectBolus);

            pstmt.executeUpdate();
            logger.info("Berechnung erfolgreich in Datenbank gespeichert."); // Logging
        } catch (SQLException e) {
            logger.error("Fehler beim Speichern der Berechnung in der Datenbank: {}", e.getMessage(), e); // Logging
            throw e; // Exception weiter werfen, damit der Controller sie behandeln kann
        } finally {
            closeConnection();
        }
    }

    public List<HourlyBolusFactor> getAllHourlyBolusFactors() throws SQLException {
        openConnection();
        List<HourlyBolusFactor> factors = new ArrayList<>();
        String sql = "SELECT hour, bolus_factor FROM hourly_bolus_factors ORDER BY hour ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                factors.add(new HourlyBolusFactor(rs.getInt("hour"), rs.getDouble("bolus_factor")));
            }
            logger.info("Stündliche Bolusfaktoren erfolgreich aus der Datenbank geladen."); // Logging
        } catch (SQLException e) {
            logger.error("Fehler beim Laden der stündlichen Bolusfaktoren aus der Datenbank: {}", e.getMessage(), e); // Logging
            throw e;
        } finally {
            closeConnection();
        }
        return factors;
    }

    public void updateHourlyBolusFactor(int hour, double bolusFactor) throws SQLException {
        openConnection();
        String sql = "INSERT INTO hourly_bolus_factors (hour, bolus_factor) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE bolus_factor = VALUES(bolus_factor), updated_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, hour);
            pstmt.setDouble(2, bolusFactor);
            pstmt.executeUpdate();
            logger.info("Bolusfaktor für Stunde {} erfolgreich in Datenbank aktualisiert auf {}.", hour, bolusFactor); // Logging
        } catch (SQLException e) {
            logger.error("Fehler beim Aktualisieren des Bolusfaktors für Stunde {}: {}", hour, e.getMessage(), e); // Logging
            throw e;
        } finally {
            closeConnection();
        }
    }

    public void initializeHourlyBolusFactors(List<HourlyBolusFactor> defaultFactors) throws SQLException {
        openConnection();
        String checkSql = "SELECT COUNT(*) FROM hourly_bolus_factors";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO hourly_bolus_factors (hour, bolus_factor) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    for (HourlyBolusFactor factor : defaultFactors) {
                        pstmt.setInt(1, factor.hour());
                        pstmt.setDouble(2, factor.bolusFactor());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    logger.info("hourly_bolus_factors Tabelle mit Standardwerten initialisiert."); // Logging
                }
            } else {
                logger.info("hourly_bolus_factors Tabelle ist bereits befüllt, keine Initialisierung notwendig."); // Logging
            }
        } catch (SQLException e) {
            logger.error("Fehler beim Initialisieren der hourly_bolus_factors Tabelle: {}", e.getMessage(), e); // Logging
            throw e;
        } finally {
            closeConnection();
        }
    }
}
