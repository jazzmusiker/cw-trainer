#cw.md

## Allgemeines

- Titel der Anwendung: CW-Trainer
- CW-Trainer ist eine Anwendung zum Lernen und Verfestigen der Morse Kenntnisse


## Technologie Stack

- Die Anwendung ist eine Jetpack Compose Multiplattform Anwendung für Android, MAC und Linux

## GUI Aufbau

- Die GUI hat einen Schwarzen Hintergrund und verwendet generell Gelbe Schriften

- Die GUI besteht aus einer Header Zeile (Oranger Hintergrund mit Schwarzer Schrift) und einem Content Bereich

- Header-Bereich enthält: 
  * Titel der Anwendung
  * eine Selektion Box für Auswahl eines Profiles
  * Button zum Hinzufügen eines Profils.
  * Ein Toggle Button 'drei punkte Menü' und 'Training' , beim Klick auf  'drei punkte Menü' öffnet sich im Content Bereich eine Ansicht mit den Einstellungen, 
    Bei klick auf 'Training' öffnet sich im Contentbereich die Trainings Ansicht. Default ist die Trainings Ansicht im Content Bereich und der Button 'drei punkte Menü'
    Im Trainingsbildschirm zeigt der Umschalter das Drei-Punkte-Menü als Ziel an; in den Einstellungen zeigt er „Training“.
  
- Der Content Bereich enthält in der Einstellung Ansicht die Elemente zur Eingabe der Einstellungen und in der Content Ansicht
  Drei Icons als Buttons
  * Stop Button
  * Play button
  * Pause button

- Wenn der Play Button gedrückt wurde: "Training läuft"
- Wenn der Stop Button gedrückt wurde: Den vorher im Training läuft Modus gemorsten Text.
- Wenn der Pause Button gedrückt wurde: "Training pausiert" und die verbleibende Restzeit  


## Funktionalität

# Einstellungen

- Die Einstellungen gelten für ein Profil, Profil ist gleichbedeutend mit einem Satz an Einstellungen. 

- Folgende Werte können eingestellt werden:
  * Liste der Morsezeichen paramListMorsecharacters (array ) , als Liste Lazy column bzw. Scrollbarer Dialog, alle im Training verwendeten Morsezeichen, 
    Jedes Zeichen wird dargestellt mit checkbox "Training" , fortlaufender Nummer, Bezeichnung des Morsezeichen, Morsezeichen
    (Beispiel: "1    a   .-") paramMorseCharacter , als Default sind alle Zeichen aktiv bzw. angeklickt
  * Mindestens ein Zeichen muß ausgewählt sein
  * Geschwindigkeit paramSpeedWpm in WPM als Slider, Wertebereich 5..30
  * Wortlänge min paramWordLengthMin als Slider, Wertebereich 1..10, wobei Wortlänge min maximal so groß sein darf wie Wortlänge max
  * Wortlänge max paramWordLengthMax als Slider, Wertebereich 1..10, wobei Wortlänge max mindestens so groß sein muß  wie Wortlänge min
  * Dauer des Trainings paramTrainingLength als Option, Werte 30s, 1min, 2min, 3min, 5min
  * Tonhöhe in Hz paramFrequencyTone als Dezimal Wert, Wertebereich 100..2000
  * Pause zw. Zeichen paramPauseLetters in Punktlängen als Slider: Wertebereich 3..20, Default ist 3
  * Pause zw. Wörtern paramPauseWords in Punktlängen als Slider: Wertebereich 7..30, Default ist 7

- Erstes Profil bzw. Default Profil "Default"
   * Alle verfügbaren Morsezeichen angeklickt
   * paramSpeedWpm = 12
   * paramWordLengthMin = 1
   * paramWordLengthMax = 5
   * paramTrainingLength = 30s
   * paramFrequencyTone = 600Hz
   * paramPauseLetters = 3
   * paramPauseWords = 7

# Morsen

- Fürs Morsen gilt: 
  * Es werden Striche und Punkte gemorst.
  * Zu morsende Zeichen bestehen aus Punkten und Strichen.
  * Die Länge eines Punktes bei 12 WPM beträgt 100 ms, entsprechend der Einstellung paramSpeedWpm wird die Länge umgerechnet.
  * Eine Strichlänge beträgt 3 Punkte.
  * Zwischen aufeinander folgenden gemorsten Punkten und Strichen innerhalb desselben Morsezeichens erfolgt eine Punktlänge Pause
  * Nach zu morsenden Zeichen innerhalb des selben Wortes bzw. Gruppe erfolgt eine paramPauseLetters Punktlängen Pause, mindestens jedoch 3.
  * Nach Wörtern (Gruppen aus zufälligen Zeichen) erfolgt eine paramPauseWords Punktlängen Pause, mindestens jedoch 7.
  * Für das Training sollen nur die Zeichen verwendet werden, deren Checkbox "Training" angeklickt ist
  * Es sollen folgende Morsezeichen verwendet werden: Alle Buchstaben von a..z, alle Zahlen von 0..9, umlaute ä,ü,ö (ä=".-.-" ü="..--" ö="---."),  Sonderzeichen aus der Zeichenkette ".,-:/=?!;()"
  * Es sollen gängige internationale Verkehrszeichen wie <ka> <sk> <ar> <BT> <kn> <hh> verwendet werden. Sie sollen als zusammenhängende Prosigns ohne Buchstabenabstand gesendet werden. Es sind &#32;die üblichen CW-Prosigns gemeint.
  * Die Ausgabe der Morsezeichen erfolgt über die Standard Soundausgabe mit einem Sinuston der Frequenz paramFrequencyTone und einer Länge entsprechend der Längen für Punkte und Striche, dazwischen soll keine soundausgabe erfolgen entsprechend der Pausen.

# Training

- Im Initialzustand ist keiner der Buttons "Stop", "Play", "Pause" gedrückt. Kein Text wird unter den Buttons angezeigt.
- Wird der Button "Play" gedrückt wird das Training gestartet und der Trainingstext entsprechend der Vorgaben für das Morsen ausgegeben. Unter den Buttons wird die verbleibende Zeit in Sekunden angezeigt.
- Wird der "Pause" Button gedrückt, wird das gerade ausgegebene Zeichen noch fertig ausgegeben werden und dann das Training angehalten, die Pause wird ab der nächsten Zeichengrenze wirksam, bis dahin läuft die Restzeit, "Training pausiert" angezeigt und die verbleibende Restzeit, Drücken von "Play" setzt das Training fort.
- Wird der "Stop" Button gedrückt,  wird das gerade ausgegebene Zeichen noch fertig ausgegeben werden und dann das Training angehalten und der bis dahin ausgegebenen Text angezeigt.
- Der Trainingstext mit der ungefähren Spieldauer paramTrainingLength besteht aus Gruppen mit der mindest Länge paramWordLengthMin und maximal Länge paramWordLengthMax. Die Gruppen bestehen aus zufälligen Zeichen, als Zeichen kommen alle in den Einstellungen angeklickten Zeichen in Frage.
- Zwischen den einzelnen Gruppen, bestehend aus zufälligen Zeichen, soll entsprechend paramPauseWords pausiert werden. 
- Als Gruppengröße werden die zu sendende Zeichen gezählt, Verkehrszeichen wie z.B. <ka> oder <BT> zählen als ein Zeichen.

- Läuft die Trainingszeit ab, soll das gerade ausgegebene Zeichen noch fertig ausgegeben werden und dann das Training gestoppt werden, 
  sowie der ausgegebene Text erscheinen.




#Statistik (noch nicht umsetzen, unklar wie der Lernende seine Rückmeldung eingibt)
- Die Statistik soll global, nicht als Profil gespeichert werden
- Anzeige einer Statistik der Fehlerquote der Zeichen (zusätzlicher Button in Header Zeile)
- Die Statistik Ansicht beinhaltet ein Button zum Zurücksetzen der Statistik
- Angezeigt wird eine Tabelle
  * 1. Spalte das zu morsende Zeichen
  * 2. Spalte Die Darstellung des Zeichens als Morsecode
  * 3. Spalte Anzahl wie oft das Zeichen richtig gehört wurde
  * 4. Spalte Anzahl wie oft das Zeichen nicht gehört wurde
  * 5. Spalte Anzahl wie oft das Zeichen fälschlicherweise gehört wurde, obwohl ein anderes Zeichen ausgegeben wurde.


codex resume 01a0ecb3-136a-7c43-a153-82b4c7bdcc01