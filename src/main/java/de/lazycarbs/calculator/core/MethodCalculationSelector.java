package de.lazycarbs.calculator.core;

import de.lazycarbs.calculator.data.IntermediateBolusFactors;
import de.lazycarbs.calculator.data.MethodSelectionResult;
import de.lazycarbs.calculator.methodstrategy.*;

public class MethodCalculationSelector {

    public MethodSelectionResult selectStrategy(double mealCarbs, double usualBeCalories, IntermediateBolusFactors intermediateBolusFactors) {
        // Bedingungen für Methode B -> Supersize:
        // mehr als 7,5 BE UND Kalorien aus Fett/Eiweiß > 750:
        if(intermediateBolusFactors.beSum() > 7.5 && intermediateBolusFactors.fatProteinCalories() > 750) {
            return new MethodSelectionResult(new MethodBSupersize(),
                    String.format("\nDeine Mahlzeit entspricht einer: %n"
                                + "*** Supersize-Mahlzeit *** %n"
                                + "Denn: BEs: %.2f > 7,5 BE und Kalorien aus F/E: %.2f > 750 kcal%n",
                                intermediateBolusFactors.beSum(), intermediateBolusFactors.fatProteinCalories()));


        }

        //Bedingungen für Methode D NoCarb (reines Fett/Eiweiß-Essen):
        // Kohlenhydrate < 3g:
        else if(mealCarbs < 3) {
            return new MethodSelectionResult(new MethodDNocarb(),
                    String.format("\nDeine Mahlzeit entspricht einer: %n"
                                    + "*** reinen Fett/Eiweiß-Mahlzeit *** %n"
                                    + "Denn: KH: %.2fg < 3g%n",
                                    mealCarbs));
        }

        //Bedingungen für Methode C HighCarb (Kohlenhydrat-Essen mit kaum Fett/Eiweiß):
        // weniger Kalorien pro BE als üblich:
        else if(intermediateBolusFactors.beCalories() < usualBeCalories) {
            return new MethodSelectionResult(new MethodCHighcarb(),
                    String.format("\nDeine Mahlzeit entspricht einer: %n"
                                + "*** High-Carb-Mahlzeit *** %n"
                                + "Denn: die Kalorien pro BE: %.2f sind kleiner als deine üblichen Kcal pro BE: %.2f %n",
                                intermediateBolusFactors.beCalories(), usualBeCalories));
        }

        // Ansonsten wird Methode A CalorieSurplus angewendet, was in der Regel der Fall sein sollte:
        return new MethodSelectionResult(new MethodACalorieSurplus(),
                String.format("\nDeine Mahlzeit entspricht einer: %n"
                                + "*** Kalorienüberschuss-Mahlzeit *** %n"
                                + "Denn: die Kalorien pro BE: %.2f sind größer als deine üblichen Kcal pro BE: %.2f %n",
                        intermediateBolusFactors.beCalories(), usualBeCalories));
    }
}
