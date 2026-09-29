package de.cwtrainer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.roundToInt

private val DeepBlack = Color(0xFF090909)
private val PanelBlack = Color(0xFF151515)
private val WarmYellow = Color(0xFFFFD54F)
private val SoftYellow = Color(0xFFF4E7B0)
private val MorseOrange = Color(0xFFF29A38)
private val Muted = Color(0xFFAAA28A)

@Composable
fun CwTrainerApp() {
    val controller = remember { TrainerController() }
    val state by controller.state.collectAsState()
    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }

    MaterialTheme(
        colors = darkColors(
            primary = MorseOrange,
            primaryVariant = Color(0xFFD9781E),
            secondary = WarmYellow,
            background = DeepBlack,
            surface = PanelBlack,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = WarmYellow,
            onSurface = SoftYellow,
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = DeepBlack) {
            Column(modifier = Modifier.fillMaxSize()) {
                TrainerHeader(state, controller)
                when (state.screen) {
                    TrainerScreen.Training -> TrainingScreen(state, controller)
                    TrainerScreen.Settings -> SettingsScreen(state, controller)
                }
            }
        }
    }
}

@Composable
private fun TrainerHeader(state: TrainerUiState, controller: TrainerController) {
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var showAddProfile by remember { mutableStateOf(false) }
    val destinationLabel = if (state.screen == TrainerScreen.Training) "⋮  Einstellungen" else "▶  Training"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MorseOrange)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        if (maxWidth < 620.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "CW-Trainer",
                        color = Color.Black,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = controller::toggleScreen) {
                        Text(destinationLabel, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
                ProfileControls(
                    state = state,
                    onSelect = controller::selectProfile,
                    onAdd = { showAddProfile = true },
                    menuExpanded = profileMenuExpanded,
                    onMenuExpandedChange = { profileMenuExpanded = it },
                    compact = true,
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "CW-Trainer",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 24.dp),
                )
                ProfileControls(
                    state = state,
                    onSelect = controller::selectProfile,
                    onAdd = { showAddProfile = true },
                    menuExpanded = profileMenuExpanded,
                    onMenuExpandedChange = { profileMenuExpanded = it },
                    compact = false,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = controller::toggleScreen) {
                    Text(destinationLabel, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showAddProfile) {
        AddProfileDialog(
            onDismiss = { showAddProfile = false },
            onCreate = { name ->
                val created = controller.addProfile(name)
                if (created) showAddProfile = false
                created
            },
        )
    }
}

@Composable
private fun ProfileControls(
    state: TrainerUiState,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (compact) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            HeaderAction(onClick = { onMenuExpandedChange(true) }, text = "⌄  ${state.selectedProfile.name}")
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
            ) {
                state.profiles.forEach { profile ->
                    DropdownMenuItem(onClick = {
                        onSelect(profile.id)
                        onMenuExpandedChange(false)
                    }) {
                        Text(profile.name)
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        HeaderAction(onClick = onAdd, text = "+  Profil")
    }
}

@Composable
private fun HeaderAction(onClick: () -> Unit, text: String) {
    TextButton(onClick = onClick) {
        Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AddProfileDialog(onDismiss: () -> Unit, onCreate: (String) -> Boolean) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil hinzufügen") },
        text = {
            Column {
                Text("Ein neues Profil startet mit den Standardeinstellungen.", color = SoftYellow)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text("Profilname") },
                    singleLine = true,
                    isError = error,
                    colors = fieldColors(),
                )
                if (error) Text("Bitte einen Namen eingeben, der noch nicht verwendet wird.", color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { error = !onCreate(name) }) { Text("Erstellen", color = MorseOrange) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = SoftYellow) } },
        backgroundColor = PanelBlack,
        contentColor = SoftYellow,
    )
}

@Composable
private fun TrainingScreen(state: TrainerUiState, controller: TrainerController) {
    val active = state.status == TrainingStatus.Playing
    val paused = state.status == TrainingStatus.Paused
    val hasTranscript = state.visibleTranscript != null
    val remainingSeconds = ceil(state.remainingMillis.coerceAtLeast(0L) / 1_000.0).toLong()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val maxPanelWidth = if (maxWidth > 850.dp) 760.dp else maxWidth
        Column(
            modifier = Modifier.width(maxPanelWidth).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PanelBlack,
                shape = RoundedCornerShape(24.dp),
                elevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("HÖRGENAUIGKEIT TRAINIEREN", color = MorseOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("${state.selectedProfile.speedWpm} WPM", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    when {
                        active || paused -> {
                            Text("$minutes:${seconds.toString().padStart(2, '0')}", color = WarmYellow, fontSize = 58.sp, fontWeight = FontWeight.Light)
                            Text("verbleibende Zeit", color = Muted, fontSize = 14.sp)
                        }
                        else -> {
                            Text(
                                when (state.status) {
                                    TrainingStatus.Finished -> "Training beendet"
                                    TrainingStatus.Stopped -> "Training gestoppt"
                                    else -> "Bereit zum Training"
                                },
                                color = WarmYellow,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Zufällige Zeichen werden als Morsecode abgespielt.",
                                color = Muted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ControlButton(
                    symbol = "■",
                    label = "Stopp",
                    enabled = active || paused || hasTranscript,
                    primary = false,
                    onClick = controller::requestStop,
                )
                ControlButton(
                    symbol = "▶",
                    label = if (paused) "Fortsetzen" else "Start",
                    enabled = !active,
                    primary = true,
                    onClick = controller::startOrResume,
                )
                ControlButton(symbol = "Ⅱ", label = "Pause", enabled = active, primary = false, onClick = controller::requestPause)
            }

            when {
                active -> StatusText(text = "Training läuft · ${remainingSeconds}s verbleiben")
                paused -> StatusText(text = "Training pausiert · ${remainingSeconds}s verbleiben")
                state.message?.startsWith("Audioausgabe") == true -> StatusText(text = state.message.orEmpty())
                hasTranscript -> TranscriptPanel(state.visibleTranscript.orEmpty())
                state.message != null -> StatusText(text = state.message.orEmpty())
                else -> Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ControlButton(symbol: String, label: String, enabled: Boolean, primary: Boolean, onClick: () -> Unit) {
    val buttonColor = if (primary) MorseOrange else PanelBlack
    val contentColor = if (primary) Color.Black else WarmYellow
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(68.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = buttonColor,
                contentColor = contentColor,
                disabledBackgroundColor = PanelBlack,
                disabledContentColor = Muted.copy(alpha = 0.45f),
            ),
            elevation = ButtonDefaults.elevation(defaultElevation = if (primary) 5.dp else 0.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Text(symbol, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Text(label, color = if (enabled) Muted else Muted.copy(alpha = 0.45f), fontSize = 12.sp)
    }
}

@Composable
private fun StatusText(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(text, color = SoftYellow, fontSize = 15.sp, textAlign = TextAlign.Center)
}

@Composable
private fun TranscriptPanel(transcript: String) {
    Spacer(Modifier.height(20.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PanelBlack,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("AUSGEGEBENER TEXT", color = MorseOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(transcript, color = WarmYellow, fontSize = 19.sp, lineHeight = 28.sp)
        }
    }
}

@Composable
private fun SettingsScreen(state: TrainerUiState, controller: TrainerController) {
    val profile = state.selectedProfile
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 4.dp)) {
                Text("Einstellungen", color = WarmYellow, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("Diese Werte gelten für das Profil „${profile.name}“.", color = Muted, fontSize = 14.sp)
            }
        }
        item { SectionTitle("Training") }
        item {
            SettingSlider(
                title = "Geschwindigkeit",
                valueLabel = "${profile.speedWpm} WPM",
                value = profile.speedWpm.toFloat(),
                range = 5f..30f,
                steps = 24,
                onValueChange = { value -> controller.updateProfile { it.copy(speedWpm = value.toInt()) } },
            )
        }
        item {
            SettingSlider(
                title = "Zeichen pro Gruppe · Minimum",
                valueLabel = profile.wordLengthMin.toString(),
                value = profile.wordLengthMin.toFloat(),
                range = 1f..10f,
                steps = 8,
                onValueChange = { value ->
                    val min = value.toInt()
                    controller.updateProfile { it.copy(wordLengthMin = min, wordLengthMax = maxOf(it.wordLengthMax, min)) }
                },
            )
        }
        item {
            SettingSlider(
                title = "Zeichen pro Gruppe · Maximum",
                valueLabel = profile.wordLengthMax.toString(),
                value = profile.wordLengthMax.toFloat(),
                range = 1f..10f,
                steps = 8,
                onValueChange = { value ->
                    val max = value.toInt()
                    controller.updateProfile { it.copy(wordLengthMin = minOf(it.wordLengthMin, max), wordLengthMax = max) }
                },
            )
        }
        item {
            DurationSetting(
                seconds = profile.trainingLengthSeconds,
                onSelect = { seconds -> controller.updateProfile { it.copy(trainingLengthSeconds = seconds) } },
            )
        }
        item {
            SettingSlider(
                title = "Tonhöhe",
                valueLabel = "${(profile.frequencyHz * 10.0).roundToInt() / 10.0} Hz",
                value = profile.frequencyHz.toFloat(),
                range = 100f..2_000f,
                steps = 0,
                onValueChange = { value ->
                    val frequency = (value * 10f).roundToInt() / 10.0
                    controller.updateProfile { it.copy(frequencyHz = frequency) }
                },
            )
        }
        item {
            SettingSlider(
                title = "Pause zwischen Zeichen",
                valueLabel = "${profile.pauseBetweenCharacters} Punktlängen",
                value = profile.pauseBetweenCharacters.toFloat(),
                range = 3f..20f,
                steps = 16,
                onValueChange = { value -> controller.updateProfile { it.copy(pauseBetweenCharacters = value.toInt()) } },
            )
        }
        item {
            SettingSlider(
                title = "Pause zwischen Gruppen",
                valueLabel = "${profile.pauseBetweenGroups} Punktlängen",
                value = profile.pauseBetweenGroups.toFloat(),
                range = 7f..30f,
                steps = 22,
                onValueChange = { value -> controller.updateProfile { it.copy(pauseBetweenGroups = value.toInt()) } },
            )
        }
        item { SectionTitle("Morsezeichen · ${profile.enabledCharacterIds.size} ausgewählt") }
        item {
            Text(
                "Wähle mindestens ein Zeichen. Prosigns werden als zusammenhängende Zeichen gesendet.",
                color = Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        itemsIndexed(MorseCharacters.all, key = { _, character -> character.id }) { index, character ->
            CharacterSettingRow(
                number = index + 1,
                character = character,
                checked = character.id in profile.enabledCharacterIds,
                onCheckedChange = { checked ->
                    val selected = profile.enabledCharacterIds.toMutableSet()
                    if (checked) selected += character.id else if (selected.size > 1) selected -= character.id
                    controller.updateProfile { it.copy(enabledCharacterIds = selected.toList()) }
                },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = MorseOrange,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun SettingSlider(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBlack, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = SoftYellow, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(valueLabel, color = WarmYellow, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value.coerceIn(range),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MorseOrange,
                activeTrackColor = MorseOrange,
                inactiveTrackColor = Color(0xFF514938),
            ),
        )
    }
}

@Composable
private fun DurationSetting(seconds: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (seconds) {
        30 -> "30 Sekunden"
        60 -> "1 Minute"
        120 -> "2 Minuten"
        180 -> "3 Minuten"
        else -> "5 Minuten"
    }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBlack, RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Trainingsdauer", color = SoftYellow, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$label  ▾", color = WarmYellow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(30 to "30 Sekunden", 60 to "1 Minute", 120 to "2 Minuten", 180 to "3 Minuten", 300 to "5 Minuten")
                .forEach { (value, option) ->
                    DropdownMenuItem(onClick = { onSelect(value); expanded = false }) { Text(option) }
                }
        }
    }
}

@Composable
private fun CharacterSettingRow(number: Int, character: MorseCharacter, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBlack, RoundedCornerShape(12.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MorseOrange,
                uncheckedColor = Muted,
                checkmarkColor = Color.Black,
            ),
        )
        Text(number.toString(), color = Muted, fontSize = 13.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        Spacer(Modifier.width(12.dp))
        Text(character.label, color = WarmYellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(76.dp))
        Text(character.pattern, color = SoftYellow, fontSize = 16.sp, letterSpacing = 1.5.sp)
        if (character.prosign) {
            Spacer(Modifier.weight(1f))
            Text("PROSIGN", color = Muted, fontSize = 10.sp, letterSpacing = 0.8.sp)
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = SoftYellow,
    cursorColor = MorseOrange,
    focusedBorderColor = MorseOrange,
    unfocusedBorderColor = Muted,
    focusedLabelColor = MorseOrange,
    unfocusedLabelColor = Muted,
)
