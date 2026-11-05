# Feature: Gewichtete Zeichenauswahl

## Ziel und Ist-Zustand

In **Lesen üben** werden Morsezeichen aktuell gleichverteilt aus der Liste der im aktiven Profil ausgewählten Zeichen gezogen. Jedes ausgewählte Zeichen hat damit bei jeder Ziehung dieselbe Wahrscheinlichkeit.

Das Feature ergänzt eine wählbare Verteilung:

- **Gleichverteilung**: bisheriges Verhalten; alle aktivierten Zeichen haben dieselbe Wahrscheinlichkeit.
- **Gewichtung**: Zeichen werden anhand der Gewichte ihrer Zeichengruppe ausgewählt.

## Geltungsbereich

- Die Gewichtung gilt ausschließlich für **Lesen üben**.
- **Zeichen üben** bleibt unverändert: Es zieht gleichverteilt und mit Wiederholung aus den ersten bis zu acht aktivierten Zeichen des Profils.
- Der CQ-Test **CQ TEST** wird durch die Gewichtung nicht beeinflusst.
- Beide Verteilungsmodi und alle Gruppenanteile sind Einstellungen des jeweils aktiven Profils. Sie werden zusammen mit dem Profil lokal gespeichert und beim nächsten App-Start wiederhergestellt.
- Der Standardmodus für neue Profile ist **Gleichverteilung**. Bei bestehenden Profilen, die noch keinen gespeicherten Modus haben, wird ebenfalls **Gleichverteilung** angenommen, damit ihr bisheriges Verhalten erhalten bleibt.
- Die Gewichtung verändert weder die Morsezeichenauswahl des Profils noch die Statistikregeln.

## Zeichengruppen und Standardgewichte

| Gruppe | Zugeordnete Zeichen | Standardgewicht |
| --- | --- | ---: |
| Buchstaben | `a` bis `z`, `ä`, `ö`, `ü` | 70 % |
| Zahlen | `0` bis `9` | 20 % |
| Interpunktionszeichen | `.`, `,`, `-`, `:`, `/`, `?`, `!`, `;`, `(`, `)`, `=`, `+` | 8 % |
| Prosigns | `<AS>`, `<VE>`, `<KA>`, `<SK>`, `<KN>`, `<HH>` | 2 % |

Jedes verfügbare Morsezeichen gehört genau einer Gruppe an. Die Gruppenmitgliedschaft ist unabhängig davon, ob ein Zeichen im Profil aktiviert ist.

## Einstellungen

- In den Profileinstellungen gibt es einen Switch für **Gleichverteilung** und **Gewichtung**.
- Die vier Gruppenanteile werden nur im Modus **Gewichtung** angezeigt und können dort über separate Prozentregler eingestellt werden. Beim Umschalten auf **Gleichverteilung** bleiben die Werte gespeichert.
- Jeder Regler kann unabhängig von den übrigen auf einen ganzzahligen Wert von 0 bis 100 gesetzt werden. Die Werte müssen nicht zusammen 100 ergeben.
- Gruppen mit Gewicht 0 erhalten keine Ziehungswahrscheinlichkeit.
- Die Standardwerte lauten 70 %, 20 %, 8 % und 2 %.
- Änderungen werden im aktiven Profil gespeichert. Während eines laufenden **Lesen üben**-Trainings werden sie wie andere Profileinstellungen erst beim nächsten Trainingsstart verwendet.

## Ziehungslogik im Modus „Gewichtung“

Für jede einzelne zu sendende Zeichenposition gilt:

1. Die aktivierten Profilzeichen werden den vier Gruppen zugeordnet.
2. Gruppen ohne aktivierte Zeichen werden für diese Ziehung ausgelassen.
3. Gruppen mit Gewicht 0 werden ausgeschlossen. Die positiven Gewichte der verbleibenden Gruppen werden durch ihre Summe geteilt. Dadurch werden sie auf 100 % normalisiert, unabhängig davon, ob die eingegebenen Werte zusammen 100 ergeben.
4. Eine Gruppe wird anhand ihres normalisierten Anteils ausgewählt; ausgeschlossene Gruppen können nicht gezogen werden.
5. Aus den aktivierten Zeichen dieser Gruppe wird gleichverteilt ein Zeichen ausgewählt.

Jede Zeichenposition wird unabhängig gezogen; Wiederholungen sind erlaubt. Damit entspricht die langfristige Häufigkeit einer Gruppe ihrem normalisierten Anteil, während die Zeichen innerhalb einer Gruppe gleichverteilt bleiben.

### Sonderfall: Keine Gruppe mit positivem Gewicht

Wenn keine Gruppe gleichzeitig mindestens ein aktiviertes Zeichen und ein Gewicht größer 0 hat, gibt es keinen möglichen Zeichenkandidaten. Der Trainingsstart wird verhindert und die Oberfläche zeigt eine Meldung, dass mindestens eine Gruppe mit aktivierten Zeichen ein positives Gewicht benötigt. Es gibt keinen Gleichverteilungs-Fallback.

## Unverändertes Verhalten

- **Gleichverteilung** zieht direkt gleichverteilt aus allen aktivierten Zeichen, unabhängig von deren Gruppen.
- Zeichen innerhalb eines Trainings werden weiterhin zufällig und mit Wiederholung ausgewählt; Gruppengröße, Morsezeiten, Pausen und Trainingsdauer bleiben unverändert.
- **Zeichen üben**, CQ-Test, Rückmeldung und Statistik erhalten keine neue Gewichtungslogik.
- Wenn beim Wechsel in den Modus **Gewichtung** oder beim Bearbeiten der Gewichte eine Gruppe keine aktivierten Zeichen enthält, werden deren Anteil nicht an diese Gruppe vergeben; die verbleibenden Gruppen werden bei der Ziehung normalisiert.

## Abnahmekriterien

- Der Verteilungsmodus und alle vier Gewichte werden pro Profil gespeichert und nach einem Neustart wiederhergestellt.
- Neue Profile starten mit **Gleichverteilung** und den Gewichten 70/20/8/2.
- Bei **Gleichverteilung** verhält sich **Lesen üben** wie bisher.
- Bei **Gewichtung** wird zunächst die Gruppe gemäß den auf die nichtleeren Gruppen normalisierten Gewichten gezogen und danach ein aktiviertes Zeichen dieser Gruppe gleichverteilt ausgewählt.
- Leere Gruppen und Gruppen mit Gewicht 0 werden ausgelassen. Die positiven Gewichte der übrigen Gruppen werden für jede Ziehung neu normalisiert.
- Wenn alle Gruppen mit aktivierten Zeichen Gewicht 0 haben, wird kein Zeichen ausgewählt. Der Trainingsstart wird gemäß dem oben markierten Vorschlag verhindert und eine Meldung angezeigt.
- **Zeichen üben** bleibt gleichverteilt und wird nicht von den Gruppenanteilen beeinflusst.
- Der CQ-Test, die Zahl der ausgesendeten Zeichen sowie die Statistik- und Auswertungsregeln ändern sich nicht.

