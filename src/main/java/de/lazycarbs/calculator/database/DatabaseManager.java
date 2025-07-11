package de.lazycarbs.calculator.database;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Verwaltet die Datenbankverbindung und das Speichern von Berechnungsdaten in MySQL.
 */
public class DatabaseManager {

    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;
    private Connection connection;

    /**
     * Konstruktor für den DatabaseManager.
     * @param dbUrl Die JDBC-URL der MySQL-Datenbank (z.B. "jdbc:mysql://localhost:3306/lazycarbs_db").
     * @param dbUser Der Benutzername für die Datenbankverbindung.
     * @param dbPassword Das Passwort für die Datenbankverbindung.
     */
    public DatabaseManager(String dbUrl, String dbUser, String dbPassword) {
        this.DB_URL = dbUrl;
        this.DB_USER = dbUser;
        this.DB_PASSWORD = dbPassword;
    }

    /**
     * Stellt eine Verbindung zur Datenbank her.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
    }

    /**
     * Speichert die Ergebnisse einer Bolusberechnung in der Datenbank.
     * @param mealCarbs Kohlenhydrate der Mahlzeit.
     * @param mealCalories Kalorien der Mahlzeit.
     * @param usualBeCalories Übliche Kalorien pro BE.
     * @param insulinTypeCalorieCovering Insulin-Typ Kalorienabdeckung.
     * @param currentHour Aktuelle Stunde der Eingabe.
     * @param currentMinute Aktuelle Minute der Eingabe.
     * @param usualBolusFactor Berechneter Bolusfaktor für die Uhrzeit.
     * @param intermediateBolusFactors Berechnete Zwischenfaktoren.
     * @param selectedMethodName Name der ausgewählten Berechnungsmethode.
     * @param methodExplanation Erklärung zur Methodenauswahl.
     * @param methodResults Ergebnisse der Berechnungsmethode.
     * @param movementFactor Bewegungsfaktor.
     * @param finalCorrectBolus Endgültiger korrekter Bolus.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public void saveCalculation(
            double mealCarbs, double mealCalories, double usualBeCalories,
            double insulinTypeCalorieCovering, int currentHour, int currentMinute,
            double usualBolusFactor, IntermediateBolusFactors intermediateBolusFactors,
            String selectedMethodName, String methodExplanation, MethodResults methodResults,
            double movementFactor, double finalCorrectBolus) throws SQLException {

        connect(); // Verbindung mit Datenbank herstellen

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

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setDouble(2, mealCarbs);
            statement.setDouble(3, mealCalories);
            statement.setDouble(4, usualBeCalories);
            statement.setDouble(5, insulinTypeCalorieCovering);
            statement.setInt(6, currentHour);
            statement.setInt(7, currentMinute);
            statement.setDouble(8, usualBolusFactor);
            statement.setDouble(9, intermediateBolusFactors.leanBeFactor());
            statement.setDouble(10, intermediateBolusFactors.pureCarbBeFactor());
            statement.setDouble(11, intermediateBolusFactors.beSum());
            statement.setDouble(12, intermediateBolusFactors.beCalories());
            statement.setDouble(13, intermediateBolusFactors.fatProteinCalories());
            statement.setString(14, selectedMethodName);
            statement.setString(15, methodExplanation);
            statement.setDouble(16, methodResults.correctBeFactor());
            statement.setDouble(17, methodResults.calorieSurplus());
            statement.setDouble(18, methodResults.delayedCalorieBolus());
            statement.setDouble(19, methodResults.correctBolusSum());
            statement.setDouble(20, methodResults.fatProteinCalories());
            statement.setDouble(21, movementFactor);
            statement.setDouble(22, finalCorrectBolus);

            statement.executeUpdate(); // Führt den INSERT-Befehl aus
        }
    }

    /**
     * Schließt die Datenbankverbindung.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
