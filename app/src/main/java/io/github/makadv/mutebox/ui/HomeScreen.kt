package io.github.makadv.mutebox.ui

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.makadv.mutebox.data.MuteBoxSettings
import io.github.makadv.mutebox.dnd.DndController
import io.github.makadv.mutebox.domain.RuleMode
import io.github.makadv.mutebox.domain.ScheduleMath
import io.github.makadv.mutebox.mutebox
import io.github.makadv.mutebox.schedule.ScheduleManager
import io.github.makadv.mutebox.service.AlertPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenApps: () -> Unit) {
    val context = LocalContext.current
    val app = context.mutebox
    val settings by app.settings.state.collectAsState()
    val rules by app.rules.rules.collectAsState()
    var perms by remember { mutableStateOf(Permissions.read(context)) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { perms = Permissions.read(context) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        perms = Permissions.read(context)
    }
    val ringtoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val uri = res.data?.let {
                IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            app.settings.update { it.copy(soundUri = uri?.toString()) }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("MuteBox") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!perms.all) {
                PermissionsCard(perms) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
            MainToggleCard(settings.active, enabled = perms.core) { on ->
                if (on) DndController.activate(context) else DndController.deactivate(context)
            }
            val allowedApps = rules.values.count { it.mode == RuleMode.ALL }
            val people = rules.values.filter { it.mode == RuleMode.SOME }.sumOf { it.senders.size }
            RulesCard(allowedApps, people, onOpenApps)
            ScheduleCard(settings, context)
            SoundCard(settings, context) {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_ALARM)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Suono di MuteBox")
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, settings.soundUri?.let(Uri::parse))
                ringtoneLauncher.launch(intent)
            }
            DisclaimerCard()
        }
    }
}

/** Avviso d'uso: l'app non dà garanzie e i dati restano sul telefono. */
@Composable
private fun DisclaimerCard() {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Informazioni importanti", style = MaterialTheme.typography.titleMedium)
            Text(
                "MuteBox è un progetto open source gratuito, fornito \"così com'è\", senza alcuna garanzia. " +
                    "Android o il produttore del telefono possono chiuderla o bloccarla: non usarla per " +
                    "avvisi da cui dipendono salute, sicurezza o emergenze.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Privacy: le notifiche sono lette solo sul dispositivo. MuteBox non ha accesso a Internet " +
                    "e non invia, vende o condivide alcun dato.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "MuteBox non è affiliata a WhatsApp, Meta, Google o ad altre app citate: i loro marchi " +
                    "appartengono ai rispettivi proprietari.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PermissionsCard(perms: PermState, onRequestNotifications: () -> Unit) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Permessi da concedere", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            PermRow("Accesso alle notifiche (obbligatorio)", perms.listener) {
                context.startActivity(Permissions.listenerSettings())
            }
            if (!perms.listener) {
                Text(
                    "Se Android dice \"impostazione con limitazioni\": apri Info app, menu ⋮ in alto, " +
                        "\"Consenti impostazioni con limitazioni\", poi riprova.",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = { context.startActivity(Permissions.appDetails(context)) }) { Text("Apri Info app") }
            }
            PermRow("Accesso a Non disturbare (obbligatorio)", perms.dnd) {
                context.startActivity(Permissions.dndSettings())
            }
            if (Build.VERSION.SDK_INT >= 33) {
                PermRow("Mostrare notifiche (stato di MuteBox)", perms.postNotifications, onRequestNotifications)
            }
            PermRow("Sveglie esatte (per l'orario)", perms.exactAlarms) {
                context.startActivity(Permissions.exactAlarmSettings(context))
            }
            PermRow("Nessuna ottimizzazione batteria", perms.battery) {
                context.startActivity(Permissions.batterySettings(context))
            }
            Text(
                "Su Xiaomi, Samsung e Huawei abilita anche l'avvio automatico di MuteBox, " +
                    "altrimenti il sistema può chiuderla di notte.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PermRow(label: String, granted: Boolean, onGrant: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text((if (granted) "✓ " else "✗ ") + label, Modifier.weight(1f))
        if (!granted) FilledTonalButton(onClick = onGrant) { Text("Concedi") }
    }
}

@Composable
private fun MainToggleCard(active: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = if (active) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(colors = colors) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (active) "MuteBox è attivo" else "MuteBox è spento",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    when {
                        !enabled -> "Concedi i permessi obbligatori per attivarlo"
                        active -> "Tutto in silenzio, tranne le notifiche che hai scelto"
                        else -> "Le notifiche funzionano normalmente"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Switch(checked = active, onCheckedChange = onToggle, enabled = enabled || active)
        }
    }
}

@Composable
private fun RulesCard(allowedApps: Int, people: Int, onOpenApps: () -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cosa può suonare", style = MaterialTheme.typography.titleMedium)
            Text(
                if (allowedApps == 0 && people == 0) "Niente: scegli app e persone."
                else listOf(
                    if (people == 1) "1 persona" else "$people persone",
                    if (allowedApps == 1) "1 app intera" else "$allowedApps app intere",
                ).joinToString(" e "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onOpenApps, modifier = Modifier.fillMaxWidth()) { Text("Scegli app e persone") }
        }
    }
}

@Composable
private fun ScheduleCard(settings: MuteBoxSettings, context: Context) {
    val store = context.mutebox.settings
    fun pickTime(initial: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(context, { _, h, m -> onPicked(h * 60 + m) }, initial / 60, initial % 60, true).show()
    }
    fun changed() {
        ScheduleManager.reschedule(context)
        ScheduleManager.applyCurrentWindow(context)
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Orario automatico", style = MaterialTheme.typography.titleMedium)
                    Text("Si attiva e si spegne da solo", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = settings.scheduleEnabled, onCheckedChange = { on ->
                    store.update { it.copy(scheduleEnabled = on) }
                    changed()
                })
            }
            if (settings.scheduleEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dalle", Modifier.width(56.dp))
                    OutlinedButton(onClick = {
                        pickTime(settings.startMinutes) { v -> store.update { it.copy(startMinutes = v) }; changed() }
                    }) { Text(ScheduleMath.format(settings.startMinutes)) }
                    Spacer(Modifier.width(16.dp))
                    Text("alle", Modifier.width(40.dp))
                    OutlinedButton(onClick = {
                        pickTime(settings.endMinutes) { v -> store.update { it.copy(endMinutes = v) }; changed() }
                    }) { Text(ScheduleMath.format(settings.endMinutes)) }
                }
            }
        }
    }
}

@Composable
private fun SoundCard(settings: MuteBoxSettings, context: Context, onPickSound: () -> Unit) {
    val store = context.mutebox.settings
    val soundName = remember(settings.soundUri) {
        settings.soundUri?.let { runCatching { RingtoneManager.getRingtone(context, Uri.parse(it))?.getTitle(context) }.getOrNull() }
            ?: "Suono di notifica predefinito"
    }
    var volume by remember(settings.volume) { mutableFloatStateOf(settings.volume.toFloat()) }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Suono delle notifiche consentite", style = MaterialTheme.typography.titleMedium)
            Text(
                "Suona sul canale sveglia: si sente anche in silenzioso. Il volume massimo dipende dal volume sveglia.",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(soundName, Modifier.weight(1f))
                TextButton(onClick = onPickSound) { Text("Cambia") }
            }
            Text("Volume: ${volume.toInt()}%")
            Slider(
                value = volume,
                onValueChange = { volume = it },
                onValueChangeFinished = { store.update { it.copy(volume = volume.toInt()) } },
                valueRange = 10f..100f,
            )
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vibrazione", Modifier.weight(1f))
                Switch(checked = settings.vibrate, onCheckedChange = { on -> store.update { it.copy(vibrate = on) } })
            }
            OutlinedButton(onClick = { AlertPlayer.play(context, null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Prova il suono")
            }
        }
    }
}
