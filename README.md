# English

## CW-Trainer

CW-Trainer plays randomly selected letters, digits, punctuation marks, and prosigns in Morse code. Listen to the transmission and review what you recognized. The review tracks correct answers, missed items, and misheard characters. You can also assess the spacing between groups.

The app runs on Android and as a desktop application on macOS and Linux. Profiles and statistics are stored locally on each device.

**Note:** The app interface is currently in German. The English labels below are translations; for example, **Settings** appears as **Einstellungen**, **Statistics** as **Statistik**, **Apply** as **Übernehmen**, and **Discard** as **Verwerfen**.

## Quick start

1. Select a profile at the top, or create one with **“+ Profile”**.
2. Open **“Settings”**, choose the Morse characters, and adjust the speed, group size, training duration, and tone if needed.
3. Return to **“Training”** and press **Start**.
4. Listen to the transmission. When training ends or you stop it, the review appears. Tap any items you misheard or missed.
5. Press **“Apply”** to add the results to statistics. Press **“Discard”** to close the review without changing statistics.

## Navigation and profiles

Use the header to switch between **Training** and **Settings**. Open the global error statistics with **Statistics**. From there, **Training** takes you back to the training screen.

Settings belong to the selected profile and are saved automatically when changed. New profiles use the default settings. You can rename or delete a profile; names must be unique and cannot be empty. The last remaining profile cannot be deleted. While training is starting, running, or paused, you cannot switch profiles or create another one. Changes to training settings during a transmission take effect the next time you start training.

## Settings

### Training

| Setting | Range | Default | Effect |
| --- | --- | --- | --- |
| **Speed** | 5–30 WPM | 12 WPM | Sets Morse timing. A dot lasts `1200 / WPM` milliseconds; a dash lasts three times as long. |
| **Characters per group · Minimum** | 1–10 | 1 | Smallest random group size. |
| **Characters per group · Maximum** | 1–10 | 5 | Largest random group size. The minimum and maximum are kept consistent. |
| **Training duration** | 30 seconds, 1, 2, 3, or 5 minutes | 30 seconds | Length of the transmission. A pre-start countdown is separate and does not reduce training time. |
| **Pause before training starts** | 0–5 seconds | 0 seconds | Wait after pressing **Start** before the first character is sent. The training screen shows a countdown during this time. |
| **Tone pitch** | 100–2000 Hz | 600 Hz | Frequency of the Morse tone. |
| **Pause between characters** | 3–20 dot lengths | 3 dot lengths | Time between characters in the same group. |
| **Pause between groups** | 7–30 dot lengths | 7 dot lengths | Time between two groups. After a complete group pause, the gap appears as `␠` in the review. |

Under **Morse characters**, choose which characters may be sent at random. All characters are enabled by default; at least one must remain enabled. Available characters include letters, German umlauts, digits, punctuation marks, and prosigns. Prosigns such as `<AR>` and `<SK>` are sent as continuous Morse sequences.

The speaker button next to the tone-pitch control plays **“CQ TEST”** using the current speed, pitch, and pauses. The training-character selection and group-size settings do not affect this test. The audio channel is opened for playback and closed afterward.

## Using the training controls

The large display and status on the training screen show the pre-start countdown, the remaining training time, or the current training state.

- **Start** begins a new training session. Any open review is discarded.
- **Pause** pauses training after the current Morse character has finished. The timer pauses as well.
- **Resume** reopens audio output and continues with the remaining time.
- **Stop** ends training. After stopping or when training ends normally, a review opens for the items that were fully transmitted.

Training stops when the app moves to the background. No audio channel remains open while idle.

## Learner input and review

After stopping or when training time runs out, the transmission is shown as selectable items. Each group appears on its own line; long groups can be scrolled horizontally. Repeated characters are separate items and can be reviewed independently.

Group gaps appear visibly as **`␠`** in the text. They are selectable too, so you can assess whether you recognized the group length or spacing correctly.

Every item is initially marked correct. Tap an item to choose one of these responses:

- **Heard correctly**: The item remains correct.
- **Not heard**: The transmitted character or group gap is counted as missed.
- **Heard as another character or as a group gap**: Choose the character or `␠` you perceived instead.

Correct items appear in green. Incorrect and missed items appear in red. For an incorrect alternative, the character you reported appears below the transmitted character; a group gap is shown as `␠`.

Press **“Apply”** to save the review once. The total counter increases by one for every fully transmitted character or group-gap item. **“Discard”** closes the open review without changing statistics; the transmitted text remains visible afterward. If you close the app while a review is open or start another training session, the unsubmitted review is lost.

## Statistics

Open **Statistics** in the header. Statistics are shared across all profiles and persist after the app restarts. They show counts, not percentages.

The table contains a row for every available Morse character and an additional **`␠`** row for group gaps. It shows:

- **Heard correctly**: Number of correctly recognized items.
- **Not heard**: Number of items that were missed or identified as something else.
- **Misheard**: Number of times this character was reported as the wrong alternative to another transmitted item.

For example, if **A** was sent and **B** was reported, **A**’s **Not heard** count and **B**’s **Misheard** count each increase by one. If a character is perceived as a group gap, `␠` is counted as the wrong alternative. If a group gap is perceived as a Morse character, the gap is counted as missed and the reported character as misheard.

The total counter includes character and group-gap items from submitted reviews. Characters that were not fully sent and group pauses that did not finish are not counted. **“Reset”** deletes all statistics and any open review after confirmation.

## Running and building

Java 17 is required. Android commands also require a configured Android SDK.

### Desktop

```sh
./gradlew :desktopApp:run
```

### Android

Install the debug app directly on a connected Android device:

```sh
./gradlew :androidApp:installDebug
```

Build a debug APK and print its path:

```sh
./build.android.sh
```

Build a debug APK and install it on a connected device:

```sh
./build_install_android.sh
```

If exactly one device is available, the script installs to it directly. If several devices are available, it asks you to select one.

The APK is stored under `androidApp/build/outputs/apk/`.

Create an installable release APK:

```sh
./release.sh
```

The version is read from `version.txt`. For `V1.0.0`, the file is named `cw_trainer_V1_0_0.apk` and is placed in the release output directory. The version is also displayed below the app title.

Build a release APK and install it on a connected device:

```sh
./release_and_install.sh
```

---

# Deutsch

## CW-Trainer

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

Eine installierbare Release-APK erstellen:

```sh
./release.sh
```

Die Version wird aus `version.txt` gelesen. Bei `V1.0.0` heißt die Datei `cw_trainer_V1_0_0.apk` und liegt im Release-Ausgabeordner. Die Version wird auch unter dem Anwendungstitel angezeigt.

Release bauen und auf einem verbundenen Gerät installieren:

```sh
./release_and_install.sh
```
