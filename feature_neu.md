# Trainingseinheit „Zeichen üben“

## Auswahl der Trainingseinheit

- Neben **Lesen üben** steht **Zeichen üben** als Trainingseinheit zur Verfügung.
- Die bisherige Anzeige **Lesen üben** im Header wird zum Auswahlelement für die Trainingseinheiten.
- Die gewählte Trainingseinheit wird global gespeichert und nach einem App-Neustart wiederhergestellt. Sie gehört nicht zu einem einzelnen Profil.

## Einstellungen und Zeichenpool

- Die Morsezeichen-Auswahl stammt aus dem aktiven Profil.
- Für den Zeichenpool werden die ausgewählten Morsezeichen in derselben Reihenfolge wie in der Einstellungsansicht verwendet. Wenn mehr als acht Zeichen ausgewählt sind, enthält der Pool nur die ersten acht. Der Pool enthält daher ein bis acht Zeichen.
- Die möglichen Antwortzeichen werden als Buttons für alle Zeichen des Pools angezeigt.
- Aus diesem Pool wird während des Trainings zufällig und mit Wiederholung jeweils ein Zeichen ausgewählt. Die Aussendung läuft bis zum Drücken von **Stop**.
- Verwendet werden die Geschwindigkeit, Tonhöhe, Zeichenpause und Startpause des aktiven Profils. Eine positive Startpause wird vor Beginn der Aussendung als Countdown angezeigt.
- Die Trainingsdauer, Gruppengröße und Gruppenpause gelten für diese Trainingseinheit nicht. Zwischen den Zeichen gilt weiterhin die eingestellte Zeichenpause; die Aussendung bildet eine zusammenhängende Wortgruppe.
- Die für eine Trainingseinheit verwendeten Profileinstellungen werden beim Start übernommen. Änderungen während des Trainings gelten für den nächsten Start.

## Bedienung und Antwortzuordnung

- Im Contentbereich werden die eingestellte Morsegeschwindigkeit, die möglichen Zeichen als Buttons, ein **Start**-Button und während der Aussendung ein **Stop**-Button angezeigt.
- Beim Start wird zunächst gegebenenfalls die Startpause abgewartet. Danach beginnt die Aussendung und der Status zeigt **Training läuft**.
- Nach jedem vollständig ausgesendeten Morsezeichen kann der Lernende während der weiteren Aussendung den Button des zuletzt vollständig ausgesendeten Zeichens drücken. Eine Eingabe vor dem ersten vollständig ausgesendeten Zeichen wird ignoriert.
- Es kann jeweils das zuletzt vollständig ausgesendete, noch nicht beantwortete Zeichen bewertet werden. Bleibt die Antwort bis zum vollständigen Ende des nächsten Morsezeichens aus, wird das unbeantwortete Zeichen als **nicht gehört** gezählt. Das nächste vollständig ausgesendete Zeichen wird dann zur aktuellen Antwort.
- Pro ausgesendetem Zeichen wird höchstens eine Antwort angenommen. Weitere Eingaben ohne ein neues vollständig ausgesendetes Zeichen werden ignoriert.
- Wird **Stop** während eines Morsezeichens gedrückt, wird dieses unvollständige Zeichen nicht gezählt. Bereits vollständig ausgesendete, aber noch unbeantwortete Zeichen zählen als **nicht gehört**.

## Zählregeln

Für jedes Zeichen des Pools werden während der Trainingseinheit drei Zähler geführt: **richtig gehört**, **nicht gehört** und **fälschlich gehört**.

- Wird das ausgesendete Zeichen richtig ausgewählt, steigt **richtig gehört** für dieses Zeichen um eins.
- Wird ein anderes Zeichen ausgewählt, steigt **nicht gehört** für das ausgesendete Zeichen und **fälschlich gehört** für das ausgewählte Zeichen jeweils um eins.
- Wird bis zum Ablauf der Antwortfrist kein Zeichen ausgewählt, steigt **nicht gehört** für das ausgesendete Zeichen um eins.
- Ein durch **Stop** unterbrochenes Zeichen erhöht keinen Zähler.
- Die Zähler werden zunächst nur für die aktuelle Trainingseinheit geführt und verändern die gespeicherte globale Statistik erst durch **Übernehmen**.

## Trainingsende und Statistik

- Nach **Stop** wird der Status **Training beendet** angezeigt. Darunter erscheint die Tabelle **Zeichen Statistik** mit einer Zeile für jedes Zeichen des Pools, auch wenn dessen Zähler noch null sind.
- Die Tabelle enthält die Spalten **Morsezeichen**, **richtig gehört**, **nicht gehört** und **fälschlich gehört**. Zähler werden je Morsezeichen zusammengefasst; mehrfach ausgesendete Zeichen verwenden dieselbe Tabellenzeile.
- Unter der Tabelle steht die Überschrift **In Statistik übernehmen** mit den Buttons **Übernehmen** und **Verwerfen**.
- **Übernehmen** addiert die Zähler zur bestehenden globalen Statistik. Der globale Gesamtzähler für gegebene Zeichen steigt um die Anzahl der vollständig ausgesendeten Morsezeichen. Die Werte werden dauerhaft gespeichert und können nur einmal übernommen werden.
- **Verwerfen** verändert die gespeicherte Statistik nicht.
- Nach **Übernehmen** oder **Verwerfen** bleibt die Tabelle als statische Anzeige bis zum nächsten Start sichtbar. Die Aktionsbuttons werden nach einer erfolgreichen Übernahme oder nach dem Verwerfen ausgeblendet.
- Ein neuer Start entfernt die vorherige Tabelle und beginnt eine neue Trainingseinheit. Noch nicht übernommene Zähler der vorherigen Einheit werden dabei verworfen.
