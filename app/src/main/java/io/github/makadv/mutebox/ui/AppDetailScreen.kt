package io.github.makadv.mutebox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.makadv.mutebox.domain.RuleMatcher
import io.github.makadv.mutebox.domain.RuleMode
import io.github.makadv.mutebox.mutebox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(packageName: String, label: String, onBack: () -> Unit) {
    val repo = LocalContext.current.mutebox.rules
    val rules by repo.rules.collectAsState()
    val seen by repo.seen.collectAsState()
    val rule = rules[packageName]
    val mode = rule?.mode ?: RuleMode.OFF
    val senders = rule?.senders.orEmpty()
    val chosen = senders.map(RuleMatcher::normalize).toSet()
    val suggestions = seen[packageName].orEmpty().filter { RuleMatcher.normalize(it.name) !in chosen }
    var newName by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(label) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") }
            },
        )
    }) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { Text("Quando MuteBox è attivo", style = MaterialTheme.typography.titleMedium) }
            item { ModeRow("In silenzio", mode == RuleMode.OFF) { repo.setMode(packageName, RuleMode.OFF) } }
            item { ModeRow("Suonano tutte le notifiche", mode == RuleMode.ALL) { repo.setMode(packageName, RuleMode.ALL) } }
            item { ModeRow("Suonano solo alcune persone o gruppi", mode == RuleMode.SOME) { repo.setMode(packageName, RuleMode.SOME) } }

            if (mode == RuleMode.SOME) {
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                item { Text("Persone e gruppi scelti", style = MaterialTheme.typography.titleMedium) }
                if (senders.isEmpty()) {
                    item { Text("Nessuno ancora.", style = MaterialTheme.typography.bodyMedium) }
                }
                items(senders, key = { "s_" + it }) { name ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { repo.removeSender(packageName, name) }) { Icon(Icons.Filled.Delete, "Rimuovi") }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nome come appare nella notifica") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = { repo.addSender(packageName, newName); newName = "" },
                            enabled = newName.isNotBlank(),
                            modifier = Modifier.padding(start = 8.dp),
                        ) { Text("Aggiungi") }
                    }
                }
                item {
                    Text(
                        "Usa il nome salvato in rubrica. Per un gruppo, scrivi il nome del gruppo: suonerà per ogni messaggio del gruppo.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                item { Text("Visti di recente nelle notifiche", style = MaterialTheme.typography.titleMedium) }
                if (suggestions.isEmpty()) {
                    item {
                        Text(
                            "Quando arrivano notifiche da questa app, qui compaiono i nomi da aggiungere con un tocco.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                items(suggestions, key = { "v_" + it.name }) { s ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { repo.addSender(packageName, s.name) }) { Icon(Icons.Filled.Add, "Aggiungi") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeRow(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected = selected, onClick = onSelect, role = Role.RadioButton).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text, Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
