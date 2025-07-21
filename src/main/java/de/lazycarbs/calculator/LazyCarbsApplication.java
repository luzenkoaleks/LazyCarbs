package de.lazycarbs.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Die Hauptklasse der Spring Boot Anwendung für den LazyCarbs Rechner.
 * Diese Klasse startet den Embedded Tomcat-Server und initialisiert den Spring-Kontext.
 */
@SpringBootApplication
@ComponentScan(basePackages = "de.lazycarbs.calculator") // Stellt sicher, dass alle Pakete gescannt werden
public class LazyCarbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LazyCarbsApplication.class, args);
    }
}