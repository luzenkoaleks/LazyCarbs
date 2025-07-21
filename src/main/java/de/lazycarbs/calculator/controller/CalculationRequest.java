package de.lazycarbs.calculator.controller;

/**
 * Datenklasse (Record) für die Anfrage an den CalculatorController.
 * Enthält alle notwendigen Eingabeparameter für die Bolus-Berechnung.
 *
 * @param mealCarbs Kohlenhydrate der Mahlzeit (g)
 * @param mealCalories Kalorien der Mahlzeit (kcal)
 * @param usualBeCalories Übliche Kalorien pro BE (kcal/BE)
 * @param insulinTypeCalorieCovering Insulin-Typ Kalorienabdeckung (150 oder 200)
 * @param currentHour Aktuelle Stunde der Eingabe (0-23)
 * @param currentMinute Aktuelle Minute der Eingabe (0-59)
 * @param movementFactor Bewegungsfaktor
 */
public record CalculationRequest(
        double mealCarbs,
        double mealCalories,
        double usualBeCalories,
        double insulinTypeCalorieCovering,
        int currentHour,
        int currentMinute,
        double movementFactor
) {}