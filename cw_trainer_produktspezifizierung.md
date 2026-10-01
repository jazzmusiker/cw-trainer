# CW-Trainer – Produktspezifikation

Dieses Dokument beschreibt den aktuellen Funktionsumfang und das Verhalten der Anwendung. Es dient als vollständige Grundlage, um den CW-Trainer neu zu erstellen.

## 1. Zweck und Plattformen

- **Name:** CW-Trainer.
- Die Anwendung dient zum Lernen und Festigen der Morsekenntnisse durch akustische Aussendung zufälliger Zeichen und anschließende Selbstauswertung.
- Technologie: Kotlin Multiplatform mit Jetpack Compose Multiplatform.
- Zielplattformen: Android sowie Desktop unter macOS und Linux.
- Profileinstellungen und übernommene Statistiken werden lokal auf dem jeweiligen Gerät gespeichert.

## 2. Oberfläche und Navigation

### Gestaltung

- Dunkler bis schwarzer Hintergrund, gelbe beziehungsweise cremefarbene Texte, orangefarbener Header und orangefarbene Hervorhebungen.
- Farbpalette: Hintergrund `#090909`, Karten `#151515`, Hauptgelb `#FFD54F`, helles Gelb `#F4E7B0`, Orange `#F29A38`, zurückhaltender Text `#AAA28A`.
- Header: oranger Hintergrund mit schwarzem Text. Er enthält den Titel **CW-Trainer**, Profilwahl, **+ Profil**, Navigation zu Einstellungen oder Training sowie die Navigation zur Statistik.
- Der Header hat 18 dp horizontale Innenabstände, 4 dp oberen und 12 dp unteren Innenabstand. Unter 620 dp verfügbarer Breite werden Profilwahl und Profilerstellung mit 4 dp Abstand zur Titelzeile in eine zweite Headerzeile gesetzt.
- Content-Bereich und Header sind vertikal mit kleinem Abstand angeordnet.
- Die Trainingsansicht erhält 20 dp horizontale Innenabstände und beginnt 6 dp unterhalb des Headers. Bei breiten Ansichten wird der zentrale Inhaltsbereich auf 760 dp begrenzt.

### Ansichten

- **Training:** Standardansicht beim Start. Enthält Status-/Timerkarte, die Bedienelemente und nach einer Aussendung den Text oder die Auswertung.
- **Einstellungen:** Profileinstellungen und Morsezeichen-Auswahl in einer scrollbar angeordneten Liste.
- **Statistik:** globale Fehlerstatistik.
- In Training führt der Navigationsbutton zu **Einstellungen**. In Einstellungen und Statistik führt er zu **Training**. Der zusätzliche Headerbutton **Statistik** ist außerhalb der Statistikansicht sichtbar.

### Statistikansicht

- Kopfzeile **Fehlerstatistik**, Gesamtzahl der gegebenen Zeichen und Gruppenabstände sowie Button **Zurücksetzen**.
- Die Tabelle enthält die fünf Statistikspalten und kann bei schmalen Ansichten horizontal gescrollt werden.
- Das Zurücksetzen verlangt eine Bestätigung.

### Trainingsansicht

- Oberhalb des Inhaltsbereichs liegt ein Abstand von 24 dp. Die Statuskarte zeigt den eingestellten WPM-Wert und abhängig vom Zustand:
  - vor dem Start: Countdown und „bis zum Start“;
  - während Training oder Pause: Restzeit im Format `m:ss` und „verbleibende Zeit“;
  - im Leerlauf bzw. nach Ende: Bereit-, beendet- oder gestoppt-Status.
- Countdown und Restzeit werden mit 32 sp dargestellt.
- Unter der Statuskarte folgt nach 16 dp eine eigene Karte mit den runden Bedienelementen **Stopp**, **Start/Fortsetzen** und **Pause** sowie Quadrat-, Play- und Pause-Symbol.
- Die drei Bedienelemente sind 68 dp groß und haben 22 dp Abstand zueinander. Die Bedienelementkarte hat zusätzlich 8 dp vertikalen Innenabstand.
- Nach der Bedienelementkarte folgt mit 16 dp Abstand der Status- oder Meldungstext.
- Während einer Audio- oder Statistikfehlermeldung wird diese unter den Bedienelementen angezeigt.
- Nach abgeschlossener bzw. gestoppter Aussendung wird die Auswertung in einer Karte angezeigt, die den verbleibenden vertikalen Platz einnimmt. Ihre Positionsliste ist vertikal scrollbar; Gruppenzeilen können horizontal gescrollt werden. Wenn keine Auswertung offen ist, aber ein Text sichtbar ist, wird dieser als statischer Text angezeigt.

## 3. Profile

- Ein Profil ist ein benannter Satz von Trainingseinstellungen und einer Morsezeichen-Auswahl.
- Das Profilmenü im Header erlaubt die Auswahl eines gespeicherten Profils. Das ausgewählte Profil wird gespeichert und beim nächsten App-Start wieder ausgewählt.
- Das erste Profil heißt **Default** und wird beim ersten Start automatisch angelegt.
- **+ Profil** öffnet einen Dialog zur Erstellung. Ein neues Profil wird direkt ausgewählt und erhält alle Standardwerte.
- Profilnamen werden getrimmt, dürfen nicht leer sein und müssen ohne Beachtung der Groß-/Kleinschreibung eindeutig sein.
- In der Einstellungsansicht kann das aktive Profil umbenannt oder gelöscht werden. Löschen muss bestätigt werden. Das letzte verbleibende Profil kann nicht gelöscht werden.
- Wird das aktive Profil gelöscht, wird ein verbleibendes Profil ausgewählt.
- Während Training startet, läuft oder pausiert oder ein CQ-Test abgespielt wird, sind Profilwahl, Profilerstellung und Löschen deaktiviert. Einstellungen dürfen geändert werden; ein laufendes Training verwendet dennoch seine beim Start gespeicherten Werte und Tonsamples. Die Änderung gilt für den nächsten Trainingsstart.
- Änderungen an Profilen werden lokal gespeichert.

## 4. Einstellungen

Alle folgenden Werte gehören zum ausgewählten Profil. Sliderwerte werden ganzzahlig gespeichert, ausgenommen die Tonhöhe, die in 0,1-Hz-Schritten angezeigt und gespeichert wird.

| Einstellung | Werte | Standard | Bedeutung |
| --- | --- | --- | --- |
| Geschwindigkeit | 5–30 WPM | 12 WPM | Morsegeschwindigkeit; bestimmt die Punkt- und Strichdauer. |
| Zeichen pro Gruppe – Minimum | 1–10 | 1 | Kleinste zufällige Gruppengröße. |
| Zeichen pro Gruppe – Maximum | 1–10 | 5 | Größte zufällige Gruppengröße. Minimum und Maximum werden beim Ändern automatisch konsistent gehalten. |
| Trainingsdauer | 30 s, 1, 2, 3 oder 5 min | 30 s | Dauer der eigentlichen Aussendung. |
| Pause vor Trainingsstart | 0–5 s | 0 s | Countdown zwischen Play/Start und dem Beginn der ersten Morseausgabe. Wird nicht von der Trainingsdauer abgezogen. |
| Tonhöhe | 100–2000 Hz | 600 Hz | Frequenz des Sinustons. |
| Pause zwischen Zeichen | 3–20 Punktlängen | 3 | Stille zwischen Zeichen derselben Gruppe. |
| Pause zwischen Gruppen | 7–30 Punktlängen | 7 | Stille zwischen zwei Gruppen. |

### Morsezeichen-Auswahl

- Die Zeichenliste ist scrollbar und zeigt Checkbox, laufende Nummer, Zeichen, Morsecode und bei Prosigns die Kennzeichnung **PROSIGN**.
- Standardmäßig sind alle Zeichen aktiviert. Mindestens ein Zeichen muss aktiviert bleiben.
- Die Trainingszeichen werden zufällig und mit Wiederholung aus den aktivierten Zeichen gewählt.
- Prosigns zählen als ein Zeichen für die Gruppengröße und werden ohne Buchstabenabstand als zusammenhängende Morsezeichen ausgegeben.

### CQ-Test

- Neben dem Tonhöhenregler befindet sich ein deutlich sichtbarer Lautsprecherbutton.
- Das Lautsprechersymbol ist 36 sp groß und liegt direkt neben der Box mit dem Tonhöhenregler, nicht neben dem Zahlenwert.
- Er sendet **CQ TEST** als zwei Gruppen („CQ“ und „TEST“) mit aktueller Geschwindigkeit, Tonhöhe und den eingestellten Pausen zwischen Zeichen und Gruppen.
- Für den Test ist es unerheblich, ob C, Q, T, E oder S in der Trainingszeichenauswahl aktiviert sind. Gruppengrößenparameter werden nicht verwendet.
- Der Test ist während eines laufenden, pausierten oder startenden Trainings deaktiviert.

## 5. Morsezeichenbestand

Die folgenden Bezeichnungen und Muster werden in den Einstellungen, bei der Aussendung und in der Rückmeldung verwendet. Groß-/Kleinschreibung der Buchstaben ändert das Morsezeichen nicht.

### Buchstaben

| Zeichen | Morsecode | Zeichen | Morsecode | Zeichen | Morsecode |
| --- | --- | --- | --- | --- | --- |
| a | `.-` | b | `-...` | c | `-.-.` |
| d | `-..` | e | `.` | f | `..-.` |
| g | `--.` | h | `....` | i | `..` |
| j | `.---` | k | `-.-` | l | `.-..` |
| m | `--` | n | `-.` | o | `---` |
| p | `.--.` | q | `--.-` | r | `.-.` |
| s | `...` | t | `-` | u | `..-` |
| v | `...-` | w | `.--` | x | `-..-` |
| y | `-.--` | z | `--..` |  |  |

### Ziffern, Umlaute, Satzzeichen und Prosigns

| Zeichen | Morsecode | Zeichen | Morsecode | Zeichen | Morsecode |
| --- | --- | --- | --- | --- | --- |
| 0 | `-----` | 1 | `.----` | 2 | `..---` |
| 3 | `...--` | 4 | `....-` | 5 | `.....` |
| 6 | `-....` | 7 | `--...` | 8 | `---..` |
| 9 | `----.` | ä | `.-.-` | ö | `---.` |
| ü | `..--` | . | `.-.-.-` | , | `--..--` |
| - | `-....-` | : | `---...` | / | `-..-.` |
| = | `-...-` | ? | `..--..` | ! | `-.-.--` |
| ; | `-.-.-.` | ( | `-.--.` | ) | `-.--.-` |
| `<KA>` | `-.-.-` | `<SK>` | `...-.-` | `<AR>` | `.-.-.` |
| `<BT>` | `-...-` | `<KN>` | `-.--.` | `<HH>` | `........` |

## 6. Aussendung und Trainingssteuerung

### Zeit- und Gruppierung

- Punktdauer in Millisekunden: ganzzahlig abgeschnittenes Ergebnis von `1200 / WPM`.
- Strichdauer: drei Punktlängen.
- Innerhalb eines Zeichens beträgt die Stille zwischen zwei aufeinanderfolgenden Punkten/Strichen eine Punktlänge.
- Zwischen Zeichen derselben Gruppe wird die konfigurierte Zeichenpause in Punktlängen eingehalten.
- Eine Gruppe besteht aus einer zufälligen inklusiven Anzahl Zeichen zwischen Minimum und Maximum.
- Zwischen Gruppen wird die konfigurierte Gruppenpause in Punktlängen eingehalten.
- Die Trainingsdauer beginnt erst nach dem Countdown und sobald die Audioausgabe für das Training geöffnet und startbereit ist.
- Pause und Stopp werden an einer Zeichengrenze wirksam: Ein begonnenes Morsezeichen wird vollständig beendet. Der Timer läuft bis zur wirksamen Pause weiter und ist während der Pause angehalten.
- Wenn die Trainingszeit während eines Zeichens endet, wird dieses Zeichen beendet und dann das Training abgeschlossen. Eine nicht vollständig abgelaufene Gruppenpause gilt nicht als ausgegebener Gruppenabstand.
- Beim Fortsetzen wird der Timer mit der zuvor verbleibenden Zeit fortgeführt.
- Ein neuer Start beginnt ein neues Training und verwirft eine offene, nicht übernommene Auswertung.
- Beim Wechsel der Anwendung in den Hintergrund wird das Training beendet.

### Bedienelemente und Status

- **Start** beginnt ein neues Training. Bei positiver Startpause zeigt die Oberfläche den Countdown. Danach beginnt die eigentliche Trainingszeit.
- **Pause** fordert eine Pause nach dem aktuellen Zeichen an. Nach Abschluss der Pause zeigt die Oberfläche „Training pausiert“ und die eingefrorene Restzeit.
- **Fortsetzen** (Play im pausierten Zustand) öffnet die Audioausgabe erneut und setzt fort.
- **Stopp** fordert das Beenden an der nächsten Zeichengrenze an.
- Nach Stopp oder Ablauf der Trainingsdauer werden vollständig gesendete Zeichen und vollständig abgelaufene Gruppenpausen zur Rückmeldung angeboten.
- Kann die Audioausgabe nicht hergestellt oder fortgesetzt werden, wird das Training gestoppt und eine Fehlermeldung angezeigt. Ein neuer Trainingsstart ist möglich.

## 7. Audioerzeugung

### Aktuelles Verfahren: vorab berechnete Punkt- und Strichsamples

**Verfahren 2 ist das aktive Verfahren** für Training und CQ-Test. Verfahren 1 unten ist nur die frühere Referenz.

- Bei Änderung von Frequenz oder WPM werden Punkt- und Strichsamples vorab berechnet und wiederverwendet. Der Samplecache ist an Frequenz und WPM gebunden.
- Samplefrequenz: 44.100 Hz; Mono; vorzeichenbehaftetes PCM mit 16 Bit.
- Die Samples werden aus einer Sinuswelle der eingestellten Frequenz erzeugt. Die Sampleanzahl ergibt sich aus der Dauer in Millisekunden, Samplefrequenz und ganzzahliger Rundung nach unten; mindestens ein Sample.
- Die ersten und letzten 96 Samples eines Tons werden mit dem Faktor 0,72 abgeschwächt, um Klicks zu verringern.
- Punkt- und Strichsamples haben die oben definierten Morsezeiten. Die Punktdauer wird auf ganze Millisekunden abgeschnitten.
- Für jedes Zeichen werden Punkt-/Strichsamples und die internen Stilleintervalle (je eine Punktlänge) zu einem PCM-Audioblock zusammengesetzt. Es gibt keine Tonwiedergabe während der Zeichen- und Gruppenpausen; diese werden separat zeitgesteuert.
- Die Wiedergabefunktion kehrt erst zurück, wenn der vollständige Block abgespielt wurde.
- Android verwendet eine gestreamte Mono-PCM-Audioausgabe. Nichtblockierendes Schreiben und Wiedergabefortschritt werden überwacht. Ein Fehler wird einmal durch Schließen und erneutes Öffnen der Ausgabe mit anschließendem Wiedergabeversuch wiederholt. Für Schreiben und Abspielen gilt eine Frist aus Blockdauer plus zwei Sekunden. Ein nicht behebbarer Fehler beendet das Training mit einer Fehlermeldung.
- Desktop verwendet eine Mono-PCM-`SourceDataLine`; der Block wird geschrieben und bis zum Ende ausgespielt. Die aktuelle Desktop-Implementierung besitzt keine separate Wiedergabezeitüberschreitung.

### Lebenszyklus des Audiokanals

- Im Leerlauf ist kein Audiokanal geöffnet. Das Vorberechnen oder Zwischenspeichern von Samples darf keinen dauerhaft geöffneten Audiokanal erfordern.
- Für das Training wird der Audiokanal nach einem etwaigen Startcountdown unmittelbar vor der ersten Morseausgabe geöffnet und für die Aussendung wiederverwendet.
- Bei Pause, Stopp, regulärem Ende, Audiofehler oder Wechsel in den Hintergrund wird der Audiokanal geschlossen.
- Beim Fortsetzen wird der Audiokanal erneut geöffnet. Das Training benutzt weiter die beim Start verwendeten Einstellungen und Samples.
- Für **CQ TEST** wird der Kanal unmittelbar vor der Testausgabe geöffnet und nach der Ausgabe geschlossen.

### Früheres Verfahren 1 (Referenz, nicht aktiv)

- Das frühere Verfahren rief für jeden Ton eine Funktion `playTone(frequencyHz, durationMillis)` auf und berechnete Samples pro Aufruf neu.
- Sampleformat, Sinuswelle und Dämpfung der ersten und letzten 96 Samples entsprechen dem oben beschriebenen Format. Die Anwendung soll für aktuelle Aussendungen Verfahren 2 nutzen.

## 8. Lernenden-Eingabe und Auswertung

### Darstellung

- Nach Stopp oder Trainingsende wird jede vollständig ausgegebene Zeichenposition einzeln auswählbar angezeigt. Wiederholte Zeichen sind separate Positionen.
- Gruppen stehen untereinander; jede Gruppenzeile kann horizontal gescrollt werden.
- Nach jeder vollständig abgelaufenen Pause zwischen zwei Gruppen wird genau eine Gruppenabstandsposition hinzugefügt. Sie wird in der Textansicht als sichtbares `␠` angezeigt und ist in der Auswertung ebenfalls einzeln auswählbar.
- Ein Gruppenabstand wird nicht hinzugefügt, wenn die Pause vorzeitig durch Stopp oder Trainingsende unterbrochen wurde.
- Der aufgezeichnete Text trennt Gruppen mit zwei Leerzeichen; in der statischen Textansicht werden diese als `␠` sichtbar gemacht.
- Die Reviewtokens sind anklickbare Karten. Richtige Positionen erscheinen grün. Falsche sowie nicht gehörte Positionen erscheinen rot. Eine falsche Alternative wird unter der ausgesendeten Position ohne den Zusatz „Gehört:“ angezeigt; eine nicht gehörte Position zeigt „Nicht gehört“.
- Über der Auswertebox steht die Überschrift **Auswertung**. Darunter stehen nebeneinander die Buttons **Übernehmen** und **Verwerfen**; die Liste mit den auswählbaren Positionen belegt den verbleibenden Platz und ist scrollbar.

### Antwortdialog

- Alle Positionen sind anfangs als richtig markiert. Nur vollständig ausgesendete Morsezeichen und vollständig abgelaufene Gruppenpausen werden aufgenommen.
- Beim Antippen kann der Lernende wählen:
  1. **Richtig gehört** – setzt die Position auf korrekt.
  2. **Nicht gehört** – markiert die ausgesendete Position als nicht gehört.
  3. Eine Alternative aus der Zeichenliste einschließlich Prosigns auswählen. Für ein Morsezeichen gehört die Alternative `␠` ebenfalls zur Auswahl. Für einen Gruppenabstand kann jedes Morsezeichen als gehörte Alternative gewählt werden.
- Ausgegebene Zeichen und Positionen der Rückmeldung werden unabhängig voneinander bewertet.
- **Übernehmen** speichert die Auswertung genau einmal und addiert ihre Ergebnisse global zur Statistik.
- **Verwerfen** schließt die offene Rückmeldung ohne Statistikänderung. Der ausgegebene Text bleibt danach statisch sichtbar.
- Wird die App während einer offenen Auswertung neu gestartet, bleiben gespeicherte Statistiken erhalten; Text und noch nicht übernommene Markierungen gehen verloren.
- Ein neuer Trainingsstart und das Zurücksetzen der Statistik verwerfen ebenfalls eine offene Auswertung.

### Zählregeln

- Wird Position A korrekt gehört, steigt `richtig gehört` für A um eins.
- Wird A nicht gehört, steigt `nicht gehört` für A um eins.
- Wird A als B gehört, steigt `nicht gehört` für A und `fälschlich gehört` für B um jeweils eins.
- Wird ein Morsezeichen als `␠` gehört, zählt das Morsezeichen als nicht gehört und `␠` als fälschlich gehört.
- Wird `␠` als Morsezeichen B gehört, zählt `␠` als nicht gehört und B als fälschlich gehört.
- Die Gesamtzahl steigt bei Übernahme für jede Zeichen- und Gruppenabstandsposition genau um eins, unabhängig davon, ob sie richtig, nicht gehört oder falsch gehört wurde.

## 9. Statistik

- Die Fehlerstatistik ist global und nicht an Profile gebunden. Sie wird lokal gespeichert und übersteht einen App-Neustart.
- Die Statistikansicht ist über **Statistik** im Header erreichbar und enthält eine Tabelle mit allen verfügbaren Morsezeichen sowie einer zusätzlichen Zeile `␠` für Gruppenabstände.
- Tabellenfelder: Position/Zeichen, Morsecode (für `␠`: „Gruppenabstand“), richtig gehört, nicht gehört, fälschlich gehört.
- Es werden absolute Zählwerte, keine Prozente, angezeigt.
- Der Gesamtzähler zeigt die Zahl der übernommenen Zeichen- und Gruppenabstandspositionen.
- Ein Zurücksetzen muss bestätigt werden. Es löscht alle Zähler, den Gesamtzähler und eine offene, noch nicht übernommene Auswertung.
- Fehler beim Speichern oder Zurücksetzen werden in der Oberfläche gemeldet; nicht gespeicherte Auswertungen bleiben offen und werden nicht als übernommen angezeigt.

## 10. Speicherung und Sitzungslebenszyklus

- Android: Profile und Statistiken werden getrennt in lokalem `SharedPreferences`-Speicher gesichert.
- Desktop: Profile und Statistiken werden getrennt in lokalen Benutzerpräferenzen gesichert.
- Profile einschließlich ihrer ausgewählten Zeichen und des aktiven Profil-IDs bleiben nach App-Neustart erhalten.
- Statistiken bleiben nach App-Neustart erhalten und werden beim Übernehmen bzw. Zurücksetzen gespeichert.
- Ein laufendes Training und eine offene Rückmeldung werden nicht wiederhergestellt. Nach App-Neustart beginnt die Anwendung im Trainings-Leerlauf; nicht übernommene Rückmeldung geht verloren.

## 11. Bauen und Starten

- Voraussetzung: JDK 17; für Android zusätzlich ein eingerichtetes Android SDK.
- Desktop starten: `./gradlew :desktopApp:run`
- Android-Debugversion installieren: `./gradlew :androidApp:installDebug`
- Debug-APK erzeugen und Speicherort ausgeben: `./build.android.sh`
- Debug-APK erzeugen und per ADB installieren: `./build_install_android.sh`. Bei genau einem verfügbaren Gerät wird direkt installiert; bei mehreren Geräten fragt das Skript nach dem Zielgerät.
- Die APK wird unter `androidApp/build/outputs/apk/` erzeugt.
