
# LazyCarbs Bolus-Rechner



Ein Java-Konsolenprogramm zur Berechnung des korrigierten Insulin-Bolus für Mahlzeiten, 
basierend auf Kohlenhydraten, Kalorien, individuellen Faktoren und der aktuellen Uhrzeit. 
Dieses Tool hilft Diabetikern, ihre Insulinabgabe präziser an verschiedene Mahlzeitenarten anzupassen.

## Inhaltsverzeichnis

- [Über das Projekt](#über-das-projekt)
- [Funktionen](#funktionen)
- [Installation](#installation)
- [Nutzung](#nutzung)
- [Berechnungsmethoden](#berechnungsmethoden)
- [Beispiel-Szenario](#beispiel-szenario)
- [Beitrag](#beitrag)
- [Lizenz](#lizenz)
- [Kontakt](#kontakt)

## Über das Projekt

Dieses Projekt ist eine Konsolenanwendung, die entwickelt wurde, um die Komplexität der Insulinberechnung für verschiedene Mahlzeiten zu vereinfachen. 
Es berücksichtigt nicht nur die Kohlenhydrate, sondern auch den Kaloriengehalt, den Fett- und Proteingehalt sowie tageszeitabhängige Insulinbedarfsfaktoren, 
um einen angepassten Bolusvorschlag zu liefern.

## Funktionen

* **Interaktive Eingabe:** Fordert den Benutzer zur Eingabe relevanter Mahlzeit- und persönlicher Daten auf.
* **Tageszeitabhängiger Bolusfaktor:** Berechnet den Bolusfaktor dynamisch basierend auf der eingegebenen Uhrzeit und einem vordefinierten stündlichen Profil. (aktuell auf einen vorgegebene stündliche Bolusfaktoren begrenzt)
* **Vier Berechnungsmethoden:** Wählt automatisch eine von vier spezifischen Berechnungsmethoden (`Supersize`, `NoCarb`, `HighCarb`, `CalorieSurplus`) basierend auf den eingegebenen Kohlenhydraten & Kalorien und ihrem Verhältnis zueinander.
* **Detaillierte Begründung:** Zeigt an, welche Methode ausgewählt wurde und warum, basierend auf den eingegebenen Werten.
* **Anpassung an Bewegungsfaktor:** Berücksichtigt einen zusätzlichen Bewegungsfaktor zur finalen Bolus-Anpassung.
* **Übersichtliche Ausgabe:** Präsentiert alle relevanten Zwischenwerte und Endergebnisse klar formatiert.

## Installation

Um das Projekt lokal auszuführen, benötigst du:

* **Java Development Kit (JDK) 17 oder neuer** (empfohlen).
* **Apache Maven** (oder Gradle, falls du dies bevorzugst und das Build-System entsprechend anpasst).
* **IntelliJ IDEA** (oder eine andere Java-IDE deiner Wahl).

**Schritte:**

1.  **Repository klonen:**
    ```bash
    git clone [https://github.com/luzenkoaleks/LazyCarbs.git](https://github.com/luzenkoaleks/LazyCarbs.git)
    cd LazyCarbs
    ```
2.  **Projekt in IntelliJ IDEA öffnen:**
    * Öffne IntelliJ IDEA.
    * Wähle "Open" und navigiere zum geklonten `LazyCarbsCalculator`-Ordner.
    * IntelliJ sollte das Maven-Projekt automatisch importieren.

3.  **Projekt kompilieren:**
    * Öffne das Maven-Tool-Fenster (meist rechts in IntelliJ).
    * Klicke auf `Lifecycle` -> `clean` und dann auf `install`. Dies kompiliert das Projekt und erstellt eine JAR-Datei.

## Nutzung

Nach der erfolgreichen Kompilierung kannst du das Programm über deine IDE oder die Kommandozeile starten.

**In IntelliJ IDEA:**
* Navigiere zur `Main.java`-Datei im `de.lazycarbs.calculator`-Paket.
* Klicke mit der rechten Maustaste auf die `main`-Methode und wähle "Run 'Main.main()'".

**Über die Kommandozeile (nach `mvn install`):**
1.  Öffne ein Terminal oder Git Bash im Stammverzeichnis deines Projekts.
2.  Navigiere zum `target`-Ordner, wo die JAR-Datei erstellt wurde (z.B. `cd target`).
3.  Führe das Programm aus:
    ```bash
    java -jar LazyCarbsCalculator-1.0-SNAPSHOT.jar # Dateiname kann variieren
    ```
    (Ersetze `LazyCarbsCalculator-1.0-SNAPSHOT.jar` durch den tatsächlichen Namen der generierten JAR-Datei, falls abweichend.)

Das Programm wird dich dann durch die notwendigen Eingaben führen.

## Berechnungsmethoden

Das Programm wählt automatisch eine der folgenden Methoden:

* **MethodBSupersize:** Für Mahlzeiten mit hohem BE-Gehalt und vielen Kalorien aus Fett/Eiweiß.
* **MethodDNocarb:** Speziell für Mahlzeiten mit sehr wenigen Kohlenhydraten (reine Fett/Eiweiß-Mahlzeiten).
* **MethodCHighcarb:** Für kohlenhydratreiche Mahlzeiten mit relativ wenig Fett/Eiweiß.
* **MethodACalorieSurplus:** Die Standardmethode für Mahlzeiten mit Kalorienüberschuss, die nicht in die anderen Kategorien fallen.

## Beispiel-Szenario

*************************************************
*********** Willkommen bei LAZY-CARBS ***********
*************************************************

Enter the Carbs of your meal: 75,6
Enter the Calories of your meal: 1266,85
Enter your general average Calories for a BE: 105
Gib ein 150(für Analog-Insulin) oder 200(für Normalinsulin) (150 / 200): 200

Um wie viel Uhr möchtest du essen? (z.B. 14:38 = Stunde 14 : Minute 38)
Welche Stunde (0-23): 15
Welche Minute (0-59): 25

*** Deine Eingabe: ***
Kohlenhydrate der Mahlzeit: 75,60 g (= 6,30 BE)
Kalorien der Mahlzeit: 1266,85 kcal (= 201,09 kcal/BE)
Übliche Kalorien pro BE: 105 kcal/BE
Insulin-Typ Kalorienabdeckung: 200 kcal

Eingegebene Uhrzeit: 15:25 Uhr
Berechneter Bolusfaktor für eine Mahlzeit um diese Uhrzeit:  1,0100 IE

Deine Mahlzeit entspricht einer:
*** Kalorienüberschuss-Mahlzeit ***
Denn: die Kalorien pro BE: 201,09 sind größer als deine üblichen Kcal pro BE: 105,00

*** Relevante Größen für deine Mahlzeit: ***
magerer BE-Faktor: ** 0,9854 IE/BE ** (100+100 / 105 + 100 x 1,01)
eF: ** 1,4780 IE/BE ** (201 + 100 / 105 + 100 x 1,01)
Kalorischer Überschuss: 7 kcal
Sofort-Bolus: eF: 1,48 x BEs: 6,30
Verzögerter Bolus: ** 0,03 IE **  (7 / 200 x 0,99)

****** Der Ruhe-Bolus für diese Mahlzeit entspricht: ******
*** Sofort-Bolus: 9,3117 IE ***
*** Verzögerter-Bolus: 0,0337 IE über 8h ***

Gib den Bewegungs-Faktor für diese Mahlzeit ein: 0,75
Korrekter Sofort-Bolus (angepasst an Bewegungs-Faktor):  6,9838 IE

Process finished with exit code 0

## Beitrag

Beiträge sind willkommen! Wenn du Ideen für Verbesserungen oder neue Funktionen hast, öffne bitte ein Issue oder erstelle einen Pull Request.

## Lizenz

Dieses Projekt ist unter der [MIT-Lizenz](LICENSE) lizenziert. Weitere Details findest du in der `LICENSE`-Datei.

## Kontakt

Alexander Luzenko / luzenkoaleks 

