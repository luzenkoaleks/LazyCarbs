package de.lazycarbs.calculator.util;

public class BolusFactorCalculator {

    // Definiert die stündlichen Bolusfaktoren von 0 bis 23 Uhr.
    // Index entspricht der Stunde.
    private static final double[] HOURLY_FACTORS = {
            0.83, // 0:00 Uhr
            0.77, // 1:00 Uhr
            0.72, // 2:00 Uhr
            0.72, // 3:00 Uhr
            0.77, // 4:00 Uhr
            0.88, // 5:00 Uhr
            0.99, // 6:00 Uhr
            1.14, // 7:00 Uhr
            1.10, // 8:00 Uhr
            1.27, // 9:00 Uhr
            1.48, // 10:00 Uhr
            1.25, // 11:00 Uhr
            1.02, // 12:00 Uhr
            0.81, // 13:00 Uhr
            0.81, // 14:00 Uhr
            0.81, // 15:00 Uhr
            0.81, // 16:00 Uhr
            0.81, // 17:00 Uhr
            0.81, // 18:00 Uhr
            1.01, // 19:00 Uhr
            1.01, // 20:00 Uhr
            1.01, // 21:00 Uhr
            1.01, // 22:00 Uhr
            1.01  // 23:00 Uhr
    };

    /**
     * Gibt den Bolusfaktor für eine bestimmte Stunde zurück.
     * @param hour Die Stunde (0-23).
     * @return Der Bolusfaktor für die angegebene Stunde.
     * @throws IllegalArgumentException wenn die Stunde außerhalb des gültigen Bereichs liegt.
     */
    private double getFactorForHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Stunde muss zwischen 0 und 23 liegen.");
        }
        // Liest den Faktor direkt aus dem Array, da es nun alle 24 Stunden abdeckt.
        return HOURLY_FACTORS[hour];
    }

    /**
     * Berechnet den durchschnittlichen Bolusfaktor über einen bestimmten Zeitraum.
     * Die Berechnung erfolgt minutenbasiert.
     * @param startHour Die Startstunde (0-23).
     * @param startMinute Die Startminute (0-59).
     * @param durationMinutes Die Dauer des Zeitraums in Minuten.
     * @return Der durchschnittliche Bolusfaktor für den angegebenen Zeitraum.
     * @throws IllegalArgumentException wenn die Startzeit oder Dauer ungültig ist.
     */
    public double calculateAverageBolusFactor(int startHour, int startMinute, int durationMinutes) {
        if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59) {
            throw new IllegalArgumentException("Ungültige Startzeit. Stunde (0-23), Minute (0-59).");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Dauer muss positiv sein.");
        }

        double totalFactorSum = 0.0;
        int currentHour = startHour;
        int currentMinute = startMinute;

        for (int i = 0; i < durationMinutes; i++) {
            // Faktor für die aktuelle Stunde holen
            totalFactorSum += getFactorForHour(currentHour);

            // Minute inkrementieren und Stunde anpassen, wenn nötig
            currentMinute++;
            if (currentMinute >= 60) {
                currentMinute = 0;
                currentHour = (currentHour + 1) % 24; // Stunden-Wrap-around (23 -> 0)
            }
        }

        return totalFactorSum / durationMinutes;
    }
}
