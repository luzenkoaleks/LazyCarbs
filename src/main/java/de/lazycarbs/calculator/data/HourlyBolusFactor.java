package de.lazycarbs.calculator.data;

/**
 * Datenklasse (Record) zur Repräsentation eines stündlichen Bolusfaktors
 * aus der 'hourly_bolus_factors'-Tabelle.
 *
 * @param hour Die Stunde des Tages (0-23).
 * @param bolusFactor Der Bolusfaktor für diese Stunde.
 */
public record HourlyBolusFactor(int hour, double bolusFactor) {
    // Records bieten automatisch Konstruktoren, Getter, equals(), hashCode() und toString().
}
