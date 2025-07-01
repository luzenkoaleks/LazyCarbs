package de.lazycarbs.calculator.input;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner = new Scanner(System.in);

    public double readDouble(String prompt) {
        while(true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble(); // redundant, option auf nur return scanner.nextDouble();
                return value;
            }
            catch (InputMismatchException e) {
                System.out.println("Ungültige Eingabe. Bitte gib eine Zahl ein.");
                scanner.next();
            }
        }
    }

    // Methode zum Einlesen einer Ganzzahl (für Stunde/Minute)
    public int readInt(String prompt) {
        while(true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                return value;
            }
            catch (InputMismatchException e) {
                System.out.println("Ungültige Eingabe. Bitte gib eine ganze Zahl ein.");
                scanner.next();
            }
        }
    }
}
