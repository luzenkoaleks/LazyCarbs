package de.lazycarbs.calculator.data;

/**
 * Datenklasse (Record) zur Repräsentation der Kalorienfaktoren
 * aus der 'calorie_factors'-Tabelle.
 *
 * @param usualBeCalories Übliche Kalorien pro BE (kcal/BE).
 * @param insulinTypeCalorieCovering Insulin-Typ Kalorienabdeckung (z.B. 150 oder 200).
 */
public record CalorieFactors(double usualBeCalories, double insulinTypeCalorieCovering) {
    // Records bieten automatisch Konstruktoren, Getter, equals(), hashCode() und toString().
}
