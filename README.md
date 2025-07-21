LazyCarbs Bolus-Rechner (Web-Anwendung)
!(https://placehold.co/600x200/ADD8E6/000000?text=LazyCarbs+Web-Anwendung)

Eine web-basierte Anwendung zur Berechnung des korrigierten Insulin-Bolus für Mahlzeiten. Sie besteht aus einem Java Spring Boot Backend (REST API) und einem React Frontend und berücksichtigt Kohlenhydrate, Kalorien, individuelle Faktoren und die aktuelle Uhrzeit. Dieses Tool hilft Diabetikern, ihre Insulinabgabe präziser an verschiedene Mahlzeitenarten anzupassen.

Inhaltsverzeichnis
Über das Projekt

Architektur

Funktionen

Installation

Voraussetzungen

Backend (Java Spring Boot)

Frontend (React)

Nutzung

Backend starten

Frontend starten

Webanwendung im Browser

Berechnungsmethoden

Beispiel-Szenario

Beitrag

Lizenz

Kontakt

Über das Projekt
Dieses Projekt wurde von einer Konsolenanwendung zu einer modernen Webanwendung weiterentwickelt, um die Komplexität der Insulinberechnung für verschiedene Mahlzeiten zu vereinfachen. Es berücksichtigt nicht nur die Kohlenhydrate, sondern auch den Kaloriengehalt, den Fett- und Proteingehalt sowie tageszeitabhängige Insulinbedarfsfaktoren, um einen angepassten Bolusvorschlag zu liefern.

Architektur
Die Anwendung ist in zwei Hauptkomponenten unterteilt:

Backend: Ein Java Spring Boot-Anwendung, die eine RESTful API bereitstellt. Sie enthält die gesamte Berechnungslogik und die optionale Datenbankintegration.

Frontend: Eine React-Anwendung, die die Benutzeroberfläche im Webbrowser darstellt. Sie kommuniziert über HTTP-Anfragen mit dem Backend.

Funktionen
Web-Benutzeroberfläche: Eine intuitive Oberfläche im Browser zur Eingabe der Mahlzeit- und persönlichen Daten.

Tageszeitabhängiger Bolusfaktor: Berechnet den Bolusfaktor dynamisch basierend auf der eingegebenen Uhrzeit und einem vordefinierten stündlichen Profil (aktuell auf vorgegebene stündliche Bolusfaktoren begrenzt).

Vier Berechnungsmethoden: Wählt automatisch eine von vier spezifischen Berechnungsmethoden (Supersize, NoCarb, HighCarb, CalorieSurplus) basierend auf den eingegebenen Kohlenhydraten & Kalorien und ihrem Verhältnis zueinander.

Detaillierte Begründung: Zeigt an, welche Methode ausgewählt wurde und warum, basierend auf den eingegebenen Werten.

Anpassung an Bewegungsfaktor: Berücksichtigt einen zusätzlichen Bewegungsfaktor zur finalen Bolus-Anpassung.

Übersichtliche Anzeige: Präsentiert alle relevanten Zwischenwerte und Endergebnisse klar formatiert direkt in der Web-Oberfläche.

Optionale Datenbank-Speicherung: Ermöglicht die Speicherung von Berechnungsdaten (konfigurierbar im Backend).

Installation
Um das Projekt lokal auszuführen, klone das Repository und folge den Anweisungen für Backend und Frontend.

Voraussetzungen
Java Development Kit (JDK) 17 oder neuer (empfohlen).

Apache Maven (für das Java Backend).

Node.js und npm (oder Yarn) (für das React Frontend).

IntelliJ IDEA (oder eine andere Java-IDE deiner Wahl für das Backend).

Ein Texteditor oder eine IDE für das Frontend (z.B. VS Code).

Backend (Java Spring Boot)
Repository klonen:

git clone https://github.com/luzenkoaleks/LazyCarbs.git
cd LazyCarbs

Projekt in IntelliJ IDEA öffnen:

Öffne IntelliJ IDEA.

Wähle "Open" und navigiere zum geklonten LazyCarbs-Ordner.

IntelliJ sollte das Maven-Projekt automatisch importieren. Stelle sicher, dass du dich im Branch feature/web-application befindest.

Abhängigkeiten installieren und kompilieren:

Öffne das Maven-Tool-Fenster (meist rechts in IntelliJ).

Klicke auf Lifecycle -> clean und dann auf package. Dies kompiliert das Projekt und erstellt eine ausführbare JAR-Datei im target/-Ordner.

Frontend (React)
Das Frontend-Projekt sollte neben dem Backend-Projekt im Dateisystem liegen.

Navigiere zum übergeordneten Verzeichnis deines LazyCarbs-Ordners:

cd .. # Wenn du dich im LazyCarbs-Ordner befindest

Erstelle das React-Projekt:

npx create-react-app lazycarbs-frontend --template typescript
cd lazycarbs-frontend

Abhängigkeiten installieren:

npm install # oder yarn install

Nutzung
Um die vollständige Webanwendung zu nutzen, musst du sowohl das Backend als auch das Frontend starten.

Backend starten
Stelle sicher, dass du dich im Stammverzeichnis deines Backend-Projekts (LazyCarbs) befindest.

Starte die Spring Boot-Anwendung:

In IntelliJ IDEA: Navigiere zur LazyCarbsApplication.java-Datei im de.lazycarbs.calculator-Paket. Klicke mit der rechten Maustaste auf die main-Methode und wähle "Run 'LazyCarbsApplication.main()'".

Über die Kommandozeile (nach mvn package):

cd target
java -jar LazyCarbsCalculator-1.0-SNAPSHOT.jar # Dateiname kann variieren

(Ersetze LazyCarbsCalculator-1.0-SNAPSHOT.jar durch den tatsächlichen Namen der generierten JAR-Datei.)

Hinweis zur Datenbank: Wenn du die Datenbank-Speicherung aktivieren möchtest, setze die Umgebungsvariable DB_PASSWORD vor dem Starten des Backends.

Windows CMD: set DB_PASSWORD="dein_passwort"

PowerShell: $env:DB_PASSWORD="dein_passwort"

Linux/macOS: export DB_PASSWORD="dein_passwort"

Das Backend läuft standardmäßig auf http://localhost:8080.

Frontend starten
Stelle sicher, dass du dich im Stammverzeichnis deines Frontend-Projekts (lazycarbs-frontend) befindest.

Starte die React-Entwicklungsserver:

npm start # oder yarn start

Webanwendung im Browser
Nachdem sowohl das Backend als auch das Frontend gestartet sind, öffnet sich die Webanwendung automatisch in deinem Standardbrowser unter http://localhost:3000 (oder einem ähnlichen Port, den React verwendet).

Berechnungsmethoden
Das Programm wählt automatisch eine der folgenden Methoden:

MethodBSupersize: Für Mahlzeiten mit hohem BE-Gehalt und vielen Kalorien aus Fett/Eiweiß.

MethodDNocarb: Speziell für Mahlzeiten mit sehr wenigen Kohlenhydraten (reine Fett/Eiweiß-Mahlzeiten).

MethodCHighcarb: Für kohlenhydratreiche Mahlzeiten mit relativ wenig Fett/Eiweiß.

MethodACalorieSurplus: Die Standardmethode für Mahlzeiten mit Kalorienüberschuss, die nicht in die anderen Kategorien fallen.

Beispiel-Szenario
(Hier könntest du ein kurzes Beispiel einfügen, wie man die Web-Oberfläche benutzt und welche Art von Ausgabe man erwarten kann. Screenshots wären hier ideal, sobald das Frontend existiert.)

Beitrag
Beiträge sind willkommen! Wenn du Ideen für Verbesserungen oder neue Funktionen hast, öffne bitte ein Issue oder erstelle einen Pull Request.

Lizenz
Dieses Projekt ist unter der MIT-Lizenz lizenziert. Weitere Details findest du in der LICENSE-Datei.

Kontakt
Alexander Luzenko / luzenkoaleks