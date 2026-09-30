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

## Audio Generierung

Nachfolgend beschrieben werden 2. Verfahren

# Verfahren 1

- Es wird eine Funktion playTone zum Abspielen eines Tones angewendet
- Als Parameter werden übergeben frequencyHz für die Frequenz und durationMillis für die Länge des Tones in Millisekunden
- Bei jedem Aufruf der Funktion werden die Samples neu berechnet und anschließend abgespielt.
- Die Samples werden als Sinuswelle berechnet, an den ersten und letzten 96 Samples aber leicht abgeschwächt, um Klickgeräusche zu mindern.

# Verfahren 2
- Hintergrund von Verfahrne 2: Mit Verfahren 1 ist die gemorste Geschwindigkeit effektiv   
  Merklich langsamer als eingestellt. Damit bei Aufruf von playTone nicht jedes mal Rechenzeit für die Generierung verloren geht, werden die Samples nur bei Änderung der 
  Tonfrequenz oder der Geschwindigkeit vorgerechnet.
- Es werden die Audiosamples audioSamplesDot und audioSamplesDash berechnet. Die samples enthalten nur Ton. 
- Samplefrequenz, Mono-PCM-Format und Abschwächung der ersten und letzten 96 Samples sollen aus   
  Verfahren 1 übernommen werden
- Die Samples sollen erstmals beim Laden des Profils und dann immer bei Änderung der Ton Frequenz oder der Geschwindigkeit berechnet werden.
- Beim Wechsel des Profils sollen die Samples neu berechnet werden entsprechend der Einstellungen im Profil.
- Punkt- und Strichsamples werden pro Zeichen zusammen mit den internen Punktabständen zu einem Audioblock kombiniert und gemeinsam ausgegeben. Der Audiokanal wird für die Dauer einer Übertragung geöffnet und für alle Zeichen dieser Übertragung wiederverwendet.
- Verfahren 2 gilt für das Training und auch für den "CQ TEST"-Hörknopf. 
- Verfahren 2 soll die bestehende Zeitlogik übernehmen: Punktdauer `1200 / WPM` Millisekunden, Strichdauer dreimal so lang, Pausen weiterhin separat.
- Länge der Samples: Punktdauer `1200 / WPM` Millisekunden, Strichdauer dreimal so lang.
- Zeitsteuerung: Die Wiedergabe eines Zeichens kehrt zurück, wenn der gesamte Audioblock bis zum Ende abgespielt wurde.
- Die Wiedergabe darf bei einem Fehler des Audiostreams nicht unbegrenzt warten. Bei einer Zeitüberschreitung wird der Stream geschlossen, das Training mit einer Fehlermeldung beendet und ein neuer Trainingsstart ermöglicht.
- Audiokanal-Lebenszyklus: Im Leerlauf bleibt kein Audiokanal geöffnet. Für ein Training wird der Audiokanal zu Beginn der Morseausgabe geöffnet und bei Pause, Stopp, Trainingsende oder Wechsel in den Hintergrund geschlossen. Beim Fortsetzen wird er erneut geöffnet. Für "CQ TEST" wird der Audiokanal unmittelbar vor der Ausgabe geöffnet und nach deren Ende geschlossen.
- Profile und Änderungen während des Trainings: Änderungen gelten ab dem nächsten Trainingsstart; das laufende Training verwendet weiter seine Startwerte und Samples. Die Neuberechnung soll bis zum Trainingsende warten.
- Wechsel in den Hintergrund: Das laufende Training soll beendet werden.
- Wenn die Punktdauer keine ganze Millisekundenzahl ergibt, soll wie in verfahren 1 auf ganze Millisekunden abgeschnitten werden.


#Statistik

- Die Statistik soll global, nicht als Profil gespeichert werden
- Anzeige einer Fehlerstatistik der Zeichen (zusätzlicher Button in Header Zeile wie einstellungen und training )
- Die Statistik Ansicht beinhaltet einen Button zum Zurücksetzen der Statistik
- Angezeigt wird eine Tabelle
  * 1. Spalte das zu morsende Zeichen
  * 2. Spalte Die Darstellung des Zeichens als Morsecode
  * 3. Spalte Anzahl wie oft das Zeichen richtig gehört wurde
  * 4. Spalte Anzahl wie oft das Zeichen nicht gehört wurde
  * 5. Spalte Anzahl wie oft das Zeichen fälschlicherweise gehört wurde, obwohl ein anderes Zeichen ausgegeben wurde.
- Zusätzlich zu den Morsezeichen gibt es eine Zeile `␠` für den Gruppenabstand. In der Morsecode-Spalte steht dafür „Gruppenabstand“.
- Die Gesamtzahl umfasst vollständig ausgegebene Zeichen und vollständig ausgegebene Gruppenabstände.
- Fehlerstatistik: In jeder Zeile steht, wie oft die Position nicht gehört bzw. fälschlicherweise als ein anderes Zeichen oder als Gruppenabstand gehört wurde.
- Wenn A gesendet wird und B eingegeben bzw. gehört wurde werden beide Buchstaben als Fehler gezählt, A als nicht gehört und B als fälschlicherweise gehört.
- Gruppenabstände werden im ausgegebenen Text sichtbar mit `␠` dargestellt und sind wie Zeichen einzeln auswählbar. Ein Zeichen kann als `␠` markiert werden; ein Gruppenabstand kann als nicht gehört oder als ein bestimmtes Morsezeichen markiert werden.
- Ein Gruppenabstand wird als vollständig ausgegeben gewertet, sobald die Pause zwischen zwei Gruppen vollständig abgelaufen ist.
- Es sollen alle verfügbaren Zeichen angezeigt werden
- Die Rückmeldung des Lernenden soll nach dem Training erfolgen wenn der gesendete Text ausgegeben wurde und zwar so funktionieren, daß man bei den ausgegebenen text falsche Zeichen markieren kann. 
- nicht markierte Zeichen gelten als richtig
- die Statistik übersteh einen app-neustart
- Nur vollständig ausgegebene Zeichen und vollständig abgelaufene Gruppenabstände werden in die Statistik aufgenommen.
- Nach Stopp oder Trainingsende den Text als einzelne auswählbare Zeichen und Gruppenabstände anzeigen. Jede Gruppe steht in einer eigenen Zeile; lange Gruppen sind horizontal scrollbar. Wiederholte Zeichen müssen jeweils einzeln auswählbar sein.
- Jede Stelle ist zunächst als richtig gewertet. Beim Antippen kann der Lernende „Nicht gehört“ wählen oder angeben, welches andere Zeichen er gehört hat.
- Richtige Stellen werden grün angezeigt. Falsche Stellen werden rot angezeigt; bei einer gehörten Alternative wird das gehörte Zeichen eingeblendet.
- Eine gehörte Alternative wird aus der Zeichenliste ausgewählt, einschließlich der Prosigns. Bei `A → B` erhöht die Übernahme „nicht gehört“ für `A` und „fälschlich gehört“ für `B`.
- Mit **„Auswertung übernehmen“** werden die Ergebnisse einmalig gespeichert. Der Gesamtzähler erhöht sich für jede vollständig ausgegebene Zeichen- oder Gruppenabstandsposition um eins.
- **„Ausgaben verwerfen“** verwirft die offene Auswertung ohne Änderungen an der Statistik.
- Es sollen reine Zählwerte, keine Prozente angezeigt werden
- Zurücksetzen: Alle Zeichenwerte und Gesamtzähler wird gelöscht
- App-Neustart während der Auswertung: Die Statistik bleibt erhalten, nicht übernommene Auswertung mit Text und Markierungen geht verloren
-   Die Ergebnisse jeder übernommenen Auswertung werden zu den bisherigen Statistikwerten addiert. 
- Zurücksetzen verwirft eine noch nicht übernommene Auswertung
 Eine offene Auswertung wird beim Start eines neuen Trainings verworfen

Rein informativ: codex resume 01a0ecb3-136a-7c43-a153-82b4c7bdcc01
