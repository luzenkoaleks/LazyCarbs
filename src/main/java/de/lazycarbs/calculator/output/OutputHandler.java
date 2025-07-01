package de.lazycarbs.calculator.output;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodResults;
import de.lazycarbs.calculator.methodstrategy.*;

public class OutputHandler {

    // einfache String-Ausgabe:
    public void displayMessage(String message) {
        System.out.println(message);
    }

    // gibt die Anzeige des Programm-Starts zurück:
    public void displayLazyCarbs() {
        System.out.println();
        System.out.println("*************************************************");
        System.out.println("*********** Willkommen bei LAZY-CARBS ***********");
        System.out.println("*************************************************");
        System.out.println();
    }

    // gibt einen einzelnen beliebigen Wert aus (z.B. mealCarbs, beSum, etc)
    public void displayResult(String prefix, double result) {
        System.out.printf("%s %.4f IE %n", prefix, result);
    }

    // Ausgabe der User-Eingabe + Berechnungen:

    public void displayUserEntry(String prefix, double mealCarbs, double mealCalories, double usualBeCalories,
                                 double insulinTypeCalorieCovering, int currentHour, int currentMinute, double usualBolusFactor,
                                 IntermediateBolusFactors intermediateBolusFactors) {
        displayMessage(prefix);
        displayMessage(String.format("Kohlenhydrate der Mahlzeit: %.2f g (= %.2f BE)", mealCarbs, intermediateBolusFactors.beSum()));
        displayMessage(String.format("Kalorien der Mahlzeit: %.2f kcal (= %.2f kcal/BE)", mealCalories, intermediateBolusFactors.beCalories()));
        displayMessage(String.format("Übliche Kalorien pro BE: %.0f kcal/BE", usualBeCalories));
        displayMessage(String.format("Insulin-Typ Kalorienabdeckung: %.0f kcal", insulinTypeCalorieCovering));
        displayMessage(""); // Leere Zeile für bessere Lesbarkeit
        displayMessage(String.format("Eingegebene Uhrzeit: %02d:%02d Uhr", currentHour, currentMinute));
        displayResult("Berechneter Bolusfaktor für eine Mahlzeit um diese Uhrzeit: ", usualBolusFactor);
    }

    // Ausgabe der Berechnungsergebnisse mit den relevanten Werten in Abhängigkeit der gewählten Methode:
    public void displayCalculationResults(String prefix, IntermediateBolusFactors intermediateBolusFactors, MethodResults methodResults,
                                          CalculationStrategy strategy, double mealCalories, double usualBeCalories, double usualBolusFactor, double mealCarbs) {

        // System.out.println();
        System.out.println(prefix);

        if (strategy instanceof MethodBSupersize) {
            System.out.printf("purer BE-Faktor: ** %.4f IE/BE ** (50 + 100 / %.0f + 100 x %.2f) %n", intermediateBolusFactors.pureCarbBeFactor(), usualBeCalories, usualBolusFactor);
            System.out.printf("magerer BE-Faktor: ** %.4f IE/BE ** (100+100 / %.0f + 100 x %.2f)%n", intermediateBolusFactors.leanBeFactor(), usualBeCalories, usualBolusFactor);
            System.out.printf("eF: ** %.4f IE/BE ** (%.0f + 100 / %.0f + 100 x %.2f) %n", methodResults.correctBeFactor(), intermediateBolusFactors.beCalories(), usualBeCalories, usualBolusFactor);
            System.out.printf("Kalorischer Überschuss: %.0f kcal %n", methodResults.calorieSurplus());
            System.out.println("Sofort-Bolus: eF x 7,5 BEs + Überhängende BEs x purer BE-Faktor");
            System.out.printf("Verzögerter Bolus: ** %.2f IE ** (%.0f / 200 x %.2f) %n", methodResults.delayedCalorieBolus(), methodResults.calorieSurplus(), intermediateBolusFactors.leanBeFactor());
            System.out.println();
            System.out.println("****** Der Ruhe-Bolus für diese Mahlzeit entspricht: ******");
            System.out.printf("*** Sofort-Bolus: %.4f IE *** %n", methodResults.correctBolusSum());
            System.out.printf("*** Verzögerter-Bolus: %.4f IE über 8h *** %n", methodResults.delayedCalorieBolus());

        }
        else if (strategy instanceof MethodDNocarb) {
            System.out.printf("kalorische Pseudo-BEs (%.2f / 200): ** %.2f IE/BE ** %n", mealCalories, mealCalories / 200);
            System.out.printf("magerer BE-Faktor: ** %.4f IE/BE ** (100+100 / %.0f + 100 x %.2f)%n", intermediateBolusFactors.leanBeFactor(), usualBeCalories, usualBolusFactor);
            System.out.println();
            System.out.printf("Bolus über 8h: ** %.4f IE ** (%.2f x %.4f)%n", methodResults.delayedCalorieBolus(), mealCalories / 200, intermediateBolusFactors.leanBeFactor());
            System.out.println("****** Der Ruhe-Bolus für diese Mahlzeit entspricht: ******");
            System.out.printf("*** %.4f IE über 8h *** %n", methodResults.delayedCalorieBolus());

        }
        else if (strategy instanceof MethodCHighcarb) {
            System.out.printf("eF: ** %.4f IE/BE ** (%.0f + 100 / %.0f + 100 x %.2f) %n", methodResults.correctBeFactor(), intermediateBolusFactors.beCalories(), usualBeCalories, usualBolusFactor);
            System.out.printf("Sofort-Bolus: eF: %.2f x BEs: %.2f %n", methodResults.correctBeFactor(), intermediateBolusFactors.beSum());
            System.out.println();
            System.out.println("****** Der Ruhe-Bolus für diese Mahlzeit entspricht: ******");
            System.out.printf("*** Sofort-Bolus: %.4f IE *** %n", methodResults.correctBolusSum());

        }
        else if (strategy instanceof MethodACalorieSurplus) {
            System.out.printf("magerer BE-Faktor: ** %.4f IE/BE ** (100+100 / %.0f + 100 x %.2f)%n", intermediateBolusFactors.leanBeFactor(), usualBeCalories, usualBolusFactor);
            System.out.printf("eF: ** %.4f IE/BE ** (%.0f + 100 / %.0f + 100 x %.2f)%n", methodResults.correctBeFactor(), intermediateBolusFactors.beCalories(), usualBeCalories, usualBolusFactor);
            System.out.printf("Kalorischer Überschuss: %.0f kcal %n", methodResults.calorieSurplus());
            System.out.printf("Sofort-Bolus: eF: %.2f x BEs: %.2f %n", methodResults.correctBeFactor(), intermediateBolusFactors.beSum());
            System.out.printf("Verzögerter Bolus: ** %.2f IE **  (%.0f / 200 x %.2f)%n", methodResults.delayedCalorieBolus(), methodResults.calorieSurplus(), intermediateBolusFactors.leanBeFactor());
            System.out.println();
            System.out.println("****** Der Ruhe-Bolus für diese Mahlzeit entspricht: ******");
            System.out.printf("*** Sofort-Bolus: %.4f IE *** %n", methodResults.correctBolusSum());
            System.out.printf("*** Verzögerter-Bolus: %.4f IE über 8h *** %n", methodResults.delayedCalorieBolus());

        }
    }
}
