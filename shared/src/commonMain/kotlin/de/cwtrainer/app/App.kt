package de.cwtrainer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
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
        val unregisterLifecycleObserver = registerAppLifecycleObserver(controller::onAppVisibilityChanged)
        controller.onAppVisibilityChanged(true)
        onDispose {
            unregisterLifecycleObserver()
            controller.dispose()
        }
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
                    TrainerScreen.Statistics -> StatisticsScreen(state, controller)
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
    val canChangeProfiles = !state.previewing && state.status != TrainingStatus.Starting && state.status != TrainingStatus.Playing && state.status != TrainingStatus.Paused

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MorseOrange)
            .padding(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 12.dp)
    ) {
        if (maxWidth < 620.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "CW-Trainer",
                            color = Color.Black,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            AppVersion,
                            color = Color.Black.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            lineHeight = 12.sp,
                        )
                    }
                    TextButton(onClick = controller::toggleScreen) {
                        Text(destinationLabel, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                    if (state.screen != TrainerScreen.Statistics) {
                        TextButton(onClick = controller::showStatistics) {
                            Text("Statistik", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
                ProfileControls(
                    state = state,
                    onSelect = controller::selectProfile,
                    onAdd = { showAddProfile = true },
                    menuExpanded = profileMenuExpanded,
                    onMenuExpandedChange = { profileMenuExpanded = it },
                    canChangeProfiles = canChangeProfiles,
                    compact = true,
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(end = 24.dp)) {
                    Text(
                        "CW-Trainer",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        AppVersion,
                        color = Color.Black.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                    )
                }
                ProfileControls(
                    state = state,
                    onSelect = controller::selectProfile,
                    onAdd = { showAddProfile = true },
                    menuExpanded = profileMenuExpanded,
                    onMenuExpandedChange = { profileMenuExpanded = it },
                    canChangeProfiles = canChangeProfiles,
                    compact = false,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = controller::toggleScreen) {
                    Text(destinationLabel, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
                if (state.screen != TrainerScreen.Statistics) {
                    TextButton(onClick = controller::showStatistics) {
                        Text("Statistik", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
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
    canChangeProfiles: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (compact) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            HeaderAction(onClick = { onMenuExpandedChange(true) }, text = "⌄  ${state.selectedProfile.name}", enabled = canChangeProfiles)
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
        HeaderAction(onClick = onAdd, text = "+  Profil", enabled = canChangeProfiles)
    }
}

@Composable
private fun HeaderAction(onClick: () -> Unit, text: String, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled) {
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
private fun ProfileManagementCard(
    profile: TrainingProfile,
    canDelete: Boolean,
    onRename: (String) -> Boolean,
    onDelete: () -> Boolean,
) {
    var showRename by remember(profile.id) { mutableStateOf(false) }
    var showDelete by remember(profile.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PanelBlack,
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Aktives Profil", color = Muted, fontSize = 12.sp)
                    Text(profile.name, color = WarmYellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { showRename = true }) {
                    Text("✎  Umbenennen", color = MorseOrange)
                }
                TextButton(onClick = { showDelete = true }, enabled = canDelete) {
                    Text("⌫  Löschen", color = if (canDelete) Color(0xFFFF8A80) else Muted)
                }
            }
            if (!canDelete) {
                Text(
                    "Das letzte Profil kann nicht gelöscht werden. Während eines Trainings ist Löschen deaktiviert.",
                    color = Muted,
                    fontSize = 12.sp,
                )
            }
        }
    }

    if (showRename) {
        RenameProfileDialog(
            currentName = profile.name,
            onDismiss = { showRename = false },
            onRename = { name ->
                val renamed = onRename(name)
                if (renamed) showRename = false
                renamed
            },
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Profil löschen?") },
            text = { Text("„${profile.name}“ und seine Einstellungen werden gelöscht. Dieser Vorgang kann nicht rückgängig gemacht werden.", color = SoftYellow) },
            confirmButton = {
                TextButton(onClick = { if (onDelete()) showDelete = false }) {
                    Text("Löschen", color = Color(0xFFFF8A80))
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Abbrechen", color = SoftYellow) } },
            backgroundColor = PanelBlack,
            contentColor = SoftYellow,
        )
    }
}

@Composable
private fun RenameProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Boolean,
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil umbenennen") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text("Profilname") },
                    singleLine = true,
                    isError = error,
                    colors = fieldColors(),
                )
                if (error) Text("Name leer oder bereits vergeben.", color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { error = !onRename(name) }) { Text("Speichern", color = MorseOrange) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = SoftYellow) } },
        backgroundColor = PanelBlack,
        contentColor = SoftYellow,
    )
}

@Composable
private fun TrainingScreen(state: TrainerUiState, controller: TrainerController) {
    val starting = state.status == TrainingStatus.Starting
    val active = state.status == TrainingStatus.Playing
    val paused = state.status == TrainingStatus.Paused
    val hasTranscript = state.visibleTranscript != null
    val startSeconds = ceil(state.startDelayRemainingMillis.coerceAtLeast(0L) / 1_000.0).toLong()
    val remainingSeconds = ceil(state.remainingMillis.coerceAtLeast(0L) / 1_000.0).toLong()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 6.dp, bottom = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maxPanelWidth = if (maxWidth > 850.dp) 760.dp else maxWidth
        Column(
            modifier = Modifier.width(maxPanelWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PanelBlack,
                shape = RoundedCornerShape(24.dp),
                elevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${state.selectedProfile.speedWpm} WPM", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    when {
                        starting -> {
                            Text("${startSeconds}s", color = WarmYellow, fontSize = 32.sp, fontWeight = FontWeight.Light)
                            Text("bis zum Start", color = Muted, fontSize = 14.sp)
                        }
                        active || paused -> {
                            Text("$minutes:${seconds.toString().padStart(2, '0')}", color = WarmYellow, fontSize = 32.sp, fontWeight = FontWeight.Light)
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
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = PanelBlack,
                shape = RoundedCornerShape(24.dp),
                elevation = 0.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                )  {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ControlButton(
                            symbol = "■",
                            label = "Stopp",
                            enabled = starting || active || paused || hasTranscript,
                            primary = false,
                            onClick = controller::requestStop,
                        )
                        ControlButton(
                            symbol = "▶",
                            label = if (paused) "Fortsetzen" else "Start",
                            enabled = !active && !starting && !state.previewing,
                            primary = true,
                            onClick = controller::startOrResume,
                        )
                        ControlButton(
                            symbol = "Ⅱ",
                            label = "Pause",
                            enabled = active,
                            primary = false,
                            onClick = controller::requestPause
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            when {
                starting -> StatusText(text = "Morsen startet in ${startSeconds}s")
                active -> StatusText(text = "Training läuft · ${remainingSeconds}s verbleiben")
                paused -> StatusText(text = "Training pausiert · ${remainingSeconds}s verbleiben")
                state.message?.startsWith("Audioausgabe") == true -> StatusText(text = state.message.orEmpty())
                state.message?.startsWith("Statistik") == true -> StatusText(text = state.message.orEmpty())
            }
            when {
                state.pendingReview != null -> TranscriptReviewPanel(state.pendingReview, controller)
                state.visibleTranscript != null -> TranscriptPanel(state.visibleTranscript)
                state.message != null && !starting && !active && !paused && !state.message.startsWith("Audioausgabe") && !state.message.startsWith("Statistik") -> StatusText(text = state.message)
                !starting && !active && !paused && state.message == null -> Spacer(Modifier.height(48.dp))
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
            Text(transcript.replace("  ", " ␠ "), color = WarmYellow, fontSize = 19.sp, lineHeight = 28.sp)
        }
    }
}

@Composable
private fun ColumnScope.TranscriptReviewPanel(entries: List<TrainingReviewEntry>, controller: TrainerController) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val groups = remember(entries) {
        val result = mutableListOf<MutableList<Pair<Int, TrainingReviewEntry>>>()
        entries.forEachIndexed { index, entry ->
            if (result.isEmpty() || entry.startsGroup) result.add(mutableListOf())
            result.last().add(index to entry)
        }
        result.map { it.toList() }
    }

    Card(
        modifier = Modifier.fillMaxWidth().weight(1f),
        backgroundColor = PanelBlack,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
    ) {
        Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp,top = 8.dp,bottom = 2.dp)) {
            Text("Auswertung", color = MorseOrange, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = controller::commitReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MorseOrange, contentColor = Color.Black),
                ) {
                    Text("Übernehmen", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = controller::discardReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF542D2D), contentColor = SoftYellow),
                ) {
                    Text("Verwerfen", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Tippe Zeichen oder Gruppenabstände an, die du nicht oder anders gehört hast.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                itemsIndexed(groups) { _, group ->
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        group.forEach { (index, entry) ->
                            ReviewCharacterToken(entry) { selectedIndex = index }
                        }
                    }
                }
            }
        }
    }

    selectedIndex?.let { index ->
        val entry = entries.getOrNull(index)
        if (entry != null) {
            ReviewAnswerDialog(
                entry = entry,
                onCorrect = {
                    controller.markReviewAsHeard(index, entry.emittedCharacterId)
                    selectedIndex = null
                },
                onNotHeard = {
                    controller.markReviewAsNotHeard(index)
                    selectedIndex = null
                },
                onHeardAs = { characterId ->
                    controller.markReviewAsHeard(index, characterId)
                    selectedIndex = null
                },
                onDismiss = { selectedIndex = null },
            )
        }
    }
}

@Composable
private fun ReviewCharacterToken(entry: TrainingReviewEntry, onClick: () -> Unit) {
    val emitted = reviewCharacter(entry.emittedCharacterId)
    val heard = entry.heardCharacterId?.let(::reviewCharacter)
    val correct = entry.heardCharacterId == entry.emittedCharacterId
    val color = if (correct) Color(0xFF263A2B) else Color(0xFF542D2D)
    val feedback = when {
        correct -> null
        entry.heardCharacterId == null -> "Nicht gehört"
        else -> heard?.label
    }
    Card(
        modifier = Modifier.padding(end = 7.dp, bottom = 6.dp).clickable(onClick = onClick),
        backgroundColor = color,
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp,
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emitted?.label ?: entry.emittedCharacterId, color = WarmYellow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            feedback?.let { Text(it, color = SoftYellow, fontSize = 8.sp, maxLines = 1) }
        }
    }
}

@Composable
private fun ReviewAnswerDialog(
    entry: TrainingReviewEntry,
    onCorrect: () -> Unit,
    onNotHeard: () -> Unit,
    onHeardAs: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val emitted = reviewCharacter(entry.emittedCharacterId)
    val alternatives = (if (entry.emittedCharacterId == GroupSpaceStatisticId) {
        MorseCharacters.all
    } else {
        MorseCharacters.all + GroupSpaceCharacter
    }).filterNot { it.id == entry.emittedCharacterId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ausgegeben: ${emitted?.label ?: entry.emittedCharacterId}") },
        text = {
            Column {
                TextButton(onClick = onCorrect) { Text("Richtig gehört", color = WarmYellow) }
                TextButton(onClick = onNotHeard) { Text("Nicht gehört", color = WarmYellow) }
                Spacer(Modifier.height(4.dp))
                Text("Als anderes Zeichen oder als Gruppenabstand gehört:", color = Muted, fontSize = 13.sp)
                LazyColumn(Modifier.heightIn(max = 250.dp)) {
                    itemsIndexed(alternatives) { _, character ->
                        TextButton(onClick = { onHeardAs(character.id) }, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(character.label, color = WarmYellow)
                                Text(character.pattern, color = Muted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen", color = SoftYellow) } },
        backgroundColor = PanelBlack,
        contentColor = SoftYellow,
    )
}

private fun reviewCharacter(id: String): MorseCharacter? =
    if (id == GroupSpaceStatisticId) GroupSpaceCharacter else MorseCharacters.all.firstOrNull { it.id == id }

@Composable
private fun StatisticsScreen(state: TrainerUiState, controller: TrainerController) {
    var confirmReset by remember { mutableStateOf(false) }
    val horizontalScroll = rememberScrollState()
    val columnWidths = listOf(92.dp, 112.dp, 132.dp, 138.dp, 150.dp)
    val tableWidth = columnWidths.reduce { total, width -> total + width }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Fehlerstatistik", color = WarmYellow, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("Gegebene Zeichen und Gruppenabstände: ${state.statistics.totalPresentedItems}", color = Muted, fontSize = 14.sp)
            }
            TextButton(onClick = { confirmReset = true }) {
                Text("Zurücksetzen", color = MorseOrange, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (state.message?.startsWith("Statistik") == true) {
            Text(state.message, color = Color(0xFFFF8A80), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().horizontalScroll(horizontalScroll),
        ) {
            Row(Modifier.width(tableWidth).background(PanelBlack)) {
                StatisticCell("Zeichen", columnWidths[0], header = true)
                StatisticCell("Morsecode", columnWidths[1], header = true)
                StatisticCell("Richtig gehört", columnWidths[2], header = true)
                StatisticCell("Nicht gehört", columnWidths[3], header = true)
                StatisticCell("Fälschlich gehört", columnWidths[4], header = true)
            }
            Divider(color = Muted.copy(alpha = 0.45f))
            LazyColumn(Modifier.width(tableWidth).weight(1f)) {
                itemsIndexed(MorseCharacters.all + GroupSpaceCharacter, key = { _, character -> character.id }) { index, character ->
                    val counts = state.statistics.characters[character.id] ?: CharacterStatistics()
                    Row(
                        modifier = Modifier.fillMaxWidth().background(if (index % 2 == 0) PanelBlack else DeepBlack),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatisticCell(character.label, columnWidths[0])
                        StatisticCell(character.pattern, columnWidths[1])
                        StatisticCell(counts.correctlyHeard.toString(), columnWidths[2])
                        StatisticCell(counts.notHeard.toString(), columnWidths[3])
                        StatisticCell(counts.falselyHeard.toString(), columnWidths[4])
                    }
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Statistik zurücksetzen?") },
            text = { Text("Alle Zählwerte und eine offene Auswertung werden gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    controller.resetStatistics()
                    confirmReset = false
                }) { Text("Zurücksetzen", color = MorseOrange) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Abbrechen", color = SoftYellow) } },
            backgroundColor = PanelBlack,
            contentColor = SoftYellow,
        )
    }
}

@Composable
private fun StatisticCell(text: String, width: androidx.compose.ui.unit.Dp, header: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = if (header) 10.dp else 8.dp),
        color = if (header) WarmYellow else SoftYellow,
        fontSize = if (header) 12.sp else 14.sp,
        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 2,
    )
}

@Composable
private fun SettingsScreen(state: TrainerUiState, controller: TrainerController) {
    val profile = state.selectedProfile
    val canDeleteProfile = state.profiles.size > 1 && !state.previewing && state.status != TrainingStatus.Starting && state.status != TrainingStatus.Playing && state.status != TrainingStatus.Paused
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
        item {
            ProfileManagementCard(
                profile = profile,
                canDelete = canDeleteProfile,
                onRename = { name -> controller.renameProfile(profile.id, name) },
                onDelete = { controller.deleteProfile(profile.id) },
            )
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
                title = "Pause vor Trainingsstart",
                valueLabel = "${profile.pauseBeforeStartSeconds} s",
                value = profile.pauseBeforeStartSeconds.toFloat(),
                range = 0f..5f,
                steps = 4,
                onValueChange = { value -> controller.updateProfile { it.copy(pauseBeforeStartSeconds = value.toInt()) } },
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingSlider(
                    modifier = Modifier.weight(1f),
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
                IconButton(
                    onClick = controller::previewCqTest,
                    enabled = !state.previewing && state.status !in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused),
                    modifier = Modifier.padding(start = 8.dp).size(56.dp),
                ) {
                    Text("🔊", fontSize = 36.sp)
                }
            }
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
    modifier: Modifier = Modifier,
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = modifier
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
