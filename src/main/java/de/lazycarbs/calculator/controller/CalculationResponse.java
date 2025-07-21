package de.lazycarbs.calculator.controller;

/**
 * Datenklasse (Record) für die Antwort vom CalculatorController.
 * Enthält alle relevanten Ergebnisse und Statusinformationen der Bolus-Berechnung.
 *
 * @param mealCarbs Kohlenhydrate der Mahlzeit (g)
 * @param mealCalories Kalorien der Mahlzeit (kcal)
 * @param usualBeCalories Übliche Kalorien pro BE (kcal/BE)
 * @param insulinTypeCalorieCovering Insulin-Typ Kalorienabdeckung (150 oder 200)
 * @param currentHour Aktuelle Stunde der Eingabe (0-23)
 * @param currentMinute Aktuelle Minute der Eingabe (0-59)
 * @param usualBolusFactor Berechneter Bolusfaktor für die Uhrzeit
 * @param intermediateLeanBeFactor Zwischenwert: magerer BE-Faktor
 * @param intermediatePureCarbBeFactor Zwischenwert: reiner Kohlenhydrat-BE-Faktor
 * @param intermediateBeSum Zwischenwert: Summe der BEs
 * @param intermediateBeCalories Zwischenwert: Kalorien pro BE
 * @param intermediateFatProteinCalories Zwischenwert: Kalorien aus Fett/Eiweiß
 * @param methodCorrectBeFactor Ergebnis der Methode: korrekter BE-Faktor
 * @param methodCalorieSurplus Ergebnis der Methode: Kalorischer Überschuss
 * @param methodDelayedCalorieBolus Ergebnis der Methode: verzögerter Kalorien-Bolus
 * @param methodCorrectBolusSum Ergebnis der Methode: korrekte Bolus-Summe
 * @param methodFatProteinCalories Ergebnis der Methode: Kalorien aus Fett/Eiweiß (aus Methode)
 * @param movementFactor Bewegungsfaktor
 * @param finalCorrectBolus Endgültiger korrigierter Bolus
 * @param selectedMethodName Name der ausgewählten Methode
 * @param methodExplanation Erklärung zur Methodenauswahl
 * @param statusMessage Statusmeldung der Berechnung
 * @param dbStatus Status der Datenbank-Speicherung
 */
public record CalculationResponse(
        double mealCarbs,
        double mealCalories,
        double usualBeCalories,
        double insulinTypeCalorieCovering,
        int currentHour,
        int currentMinute,
        double usualBolusFactor,
        double intermediateLeanBeFactor,
        double intermediatePureCarbBeFactor,
        double intermediateBeSum,
        double intermediateBeCalories,
        double intermediateFatProteinCalories,
        double methodCorrectBeFactor,
        double methodCalorieSurplus,
        double methodDelayedCalorieBolus,
        double methodCorrectBolusSum,
        double methodFatProteinCalories,
        double movementFactor,
        double finalCorrectBolus,
        String selectedMethodName,
        String methodExplanation,
        String statusMessage,
        String dbStatus
) {}
