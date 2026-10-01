# CW-Trainer

Der CW-Trainer spielt zufällig ausgewählte Buchstaben, Ziffern, Satzzeichen und Prosigns als Morsecode ab. Du hörst die Aussendung und wertest anschließend aus, was du erkannt hast. Die Auswertung zählt richtige Antworten, nicht gehörte Positionen und Verwechslungen. Auch die Abstände zwischen Zeichengruppen kannst du beurteilen.

Die Anwendung läuft auf Android sowie als Desktop-Anwendung unter macOS und Linux. Profile und Statistik werden lokal auf dem jeweiligen Gerät gespeichert.

## Schnellstart

1. Wähle oben ein Profil oder lege mit **„+ Profil“** ein neues an.
2. Öffne **„Einstellungen“**, wähle die Morsezeichen und passe bei Bedarf Geschwindigkeit, Gruppengröße, Trainingsdauer und Ton an.
3. Gehe zurück zu **„Training“** und drücke **Start**.
4. Höre die Ausgabe. Nach Trainingsende oder Stopp erscheint die Auswertung. Tippe Positionen an, die du falsch oder nicht gehört hast.
5. Drücke **„Übernehmen“**, um die Ergebnisse zur Statistik hinzuzufügen. Mit **„Verwerfen“** schließt du die Auswertung ohne Statistikänderung.

## Navigation und Profile

Im Kopfbereich wechselst du zwischen **Training** und **Einstellungen**. Über **Statistik** öffnest du die globale Fehlerstatistik. Von dort führt **Training** zurück zur Trainingsseite.

Die Einstellungen gehören immer zum ausgewählten Profil und werden bei einer Änderung automatisch gespeichert. Neue Profile starten mit den Standardeinstellungen. Du kannst ein Profil umbenennen oder löschen; Namen müssen eindeutig und dürfen nicht leer sein. Das letzte Profil kann nicht gelöscht werden. Während eines laufenden, pausierten oder gerade startenden Trainings sind Profilwechsel und Anlegen weiterer Profile deaktiviert. Änderungen an Trainingseinstellungen während einer laufenden Aussendung werden für den nächsten Trainingsstart verwendet.

## Einstellungen

### Training

| Einstellung | Wertebereich | Standard | Wirkung |
| --- | --- | --- | --- |
| **Geschwindigkeit** | 5–30 WPM | 12 WPM | Bestimmt die Länge der Morsezeichen. Ein Punkt dauert `1200 / WPM` Millisekunden; ein Strich dauert dreimal so lang. |
| **Zeichen pro Gruppe · Minimum** | 1–10 | 1 | Kleinste zufällige Gruppengröße. |
| **Zeichen pro Gruppe · Maximum** | 1–10 | 5 | Größte zufällige Gruppengröße. Das Minimum kann nicht größer als das Maximum sein und umgekehrt. |
| **Trainingsdauer** | 30 Sekunden, 1, 2, 3 oder 5 Minuten | 30 Sekunden | Dauer der Aussendung. Ein Countdown vor dem Start wird separat behandelt und verkürzt die Trainingsdauer nicht. |
| **Pause vor Trainingsstart** | 0–5 Sekunden | 0 Sekunden | Wartezeit nach **Start**, bevor das erste Zeichen gesendet wird. Währenddessen zeigt die Trainingsseite den Countdown. |
| **Tonhöhe** | 100–2000 Hz | 600 Hz | Frequenz des Morse-Tons. |
| **Pause zwischen Zeichen** | 3–20 Punktlängen | 3 Punktlängen | Zeit zwischen zwei Zeichen innerhalb einer Gruppe. |
| **Pause zwischen Gruppen** | 7–30 Punktlängen | 7 Punktlängen | Zeit zwischen zwei Gruppen. Nach einer vollständig abgelaufenen Gruppenpause wird der Abstand in der Auswertung als `␠` dargestellt. |

Unter **Morsezeichen** wählst du aus, welche Zeichen zufällig gesendet werden dürfen. Standardmäßig sind alle Zeichen ausgewählt; mindestens ein Zeichen muss ausgewählt bleiben. Verfügbar sind Buchstaben, Umlaute, Ziffern, Satzzeichen und Prosigns. Prosigns, zum Beispiel `<AR>` oder `<SK>`, werden als zusammenhängende Morsezeichen gesendet.

Der Lautsprecherknopf neben der Tonhöhen-Einstellung spielt **„CQ TEST“** mit den aktuellen Werten für Geschwindigkeit, Tonhöhe und Pausen ab. Die Auswahl der Trainingszeichen und die Gruppengröße bestimmen diesen Test nicht. Der Audiokanal wird für die Ausgabe geöffnet und anschließend wieder geschlossen.

## Training bedienen

Auf der Trainingsseite zeigen die große Anzeige und der Status den Countdown vor dem Start, die verbleibende Trainingszeit oder den Trainingszustand.

- **Start** beginnt ein neues Training. Eine noch offene Auswertung wird dabei verworfen.
- **Pause** hält das Training nach Abschluss des aktuellen Morsezeichens an. Der Timer hält ebenfalls an.
- **Fortsetzen** öffnet die Audioausgabe wieder und setzt das Training mit dem verbleibenden Timer fort.
- **Stopp** beendet das Training. Nach Stopp oder regulärem Trainingsende wird die Auswertung für die vollständig ausgegebenen Positionen geöffnet.

Beim Wechsel der Anwendung in den Hintergrund wird ein laufendes Training beendet. Im Leerlauf bleibt kein Audiokanal geöffnet.

## Eingabe und Auswertung durch den Lernenden

Nach Stopp oder Ablauf der Trainingsdauer wird die Ausgabe als auswählbare Positionen gezeigt. Jede Gruppe steht in einer eigenen Zeile; eine lange Gruppe lässt sich horizontal scrollen. Wiederholte Zeichen sind einzelne Positionen und können unabhängig voneinander bewertet werden.

Gruppenabstände werden im Text sichtbar als **`␠`** angezeigt. Sie sind ebenfalls auswählbar. Damit kannst du beurteilen, ob du die Gruppenlänge beziehungsweise den Abstand zwischen Gruppen richtig erkannt hast.

Jede Position gilt zunächst als richtig. Tippe eine Position an, um eine der folgenden Antworten festzulegen:

- **Richtig gehört**: Die Position bleibt korrekt.
- **Nicht gehört**: Das ausgegebene Zeichen oder der Gruppenabstand wird als nicht gehört gezählt.
- **Als anderes Zeichen oder als Gruppenabstand gehört**: Wähle das Zeichen beziehungsweise `␠`, das du stattdessen wahrgenommen hast.

Korrekte Positionen erscheinen grün. Falsche und nicht gehörte Positionen erscheinen rot. Bei einer falschen Alternative steht das gehörte Zeichen unter dem gesendeten Zeichen; der Gruppenabstand wird als `␠` dargestellt.

Über **„Übernehmen“** speicherst du die Rückmeldung einmalig. Der Gesamtzähler steigt für jede vollständig ausgegebene Zeichen- oder Gruppenabstandsposition um eins. **„Verwerfen“** schließt die offene Rückmeldung ohne Statistikänderung; der ausgegebene Text bleibt anschließend sichtbar. Wenn du die App während einer offenen Auswertung schließt oder ein neues Training startest, geht die nicht übernommene Auswertung verloren.

## Statistik

Öffne **Statistik** im Kopfbereich. Die Statistik gilt gemeinsam für alle Profile und bleibt nach einem App-Neustart erhalten. Sie enthält reine Zählwerte, keine Prozentangaben.

Die Tabelle enthält eine Zeile für jedes verfügbare Morsezeichen sowie eine Zeile **`␠`** für Gruppenabstände. Angezeigt werden:

- **Richtig gehört**: Anzahl der korrekt erkannten Positionen.
- **Nicht gehört**: Anzahl der Positionen, die nicht oder als etwas anderes erkannt wurden.
- **Fälschlich gehört**: Anzahl der Male, bei denen dieses Zeichen als falsche Alternative angegeben wurde, obwohl eine andere Position ausgegeben wurde.

Wenn zum Beispiel **A** gesendet und **B** als gehörte Alternative angegeben wurde, steigt bei **A** der Zähler **Nicht gehört** und bei **B** der Zähler **Fälschlich gehört**. Wird ein Zeichen als Gruppenabstand `␠` wahrgenommen, wird entsprechend `␠` als falsche Alternative gezählt. Wird ein Gruppenabstand als Morsezeichen wahrgenommen, zählt der Abstand als nicht gehört und das angegebene Zeichen als fälschlich gehört.

Der Gesamtzähler umfasst die in übernommenen Auswertungen enthaltenen Zeichen und Gruppenabstände. Nicht vollständig gesendete Zeichen und nicht vollständig abgelaufene Gruppenpausen gehen nicht in die Auswertung ein. **„Zurücksetzen“** löscht alle Statistikwerte und eine eventuell offene Auswertung nach einer Bestätigung.

## Starten und Bauen

Voraussetzung ist eine installierte Java-17-Umgebung. Die Android-Befehle benötigen außerdem ein eingerichtetes Android SDK.

### Desktop

```sh
./gradlew :desktopApp:run
```

### Android

Debug-App direkt auf einem angeschlossenen Android-Gerät installieren:

```sh
./gradlew :androidApp:installDebug
```

Eine Debug-APK bauen und ihren Pfad ausgeben:

```sh
./build.android.sh
```

Eine Debug-APK bauen und auf einem verbundenen Gerät installieren:

```sh
./build_install_android.sh
```

Bei genau einem verfügbaren Gerät installiert das Skript direkt. Bei mehreren Geräten fragt es nach der Gerätenummer.

Die APK liegt unter `androidApp/build/outputs/apk/`.
