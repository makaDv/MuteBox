package com.hushly.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.hushly.domain.AppRule
import com.hushly.domain.RuleMode
import com.hushly.hushly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppEntry(val packageName: String, val label: String, val icon: ImageBitmap?)

/** Le app installate cambiano raramente: le carico una volta per processo. */
private object InstalledApps {
    @Volatile
    var cache: List<AppEntry>? = null

    fun load(context: Context): List<AppEntry> {
        cache?.let { return it }
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(launcher, 0)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName }
            .mapNotNull { pkg ->
                runCatching {
                    val info = pm.getApplicationInfo(pkg, 0)
                    val icon = runCatching { pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap() }.getOrNull()
                    AppEntry(pkg, pm.getApplicationLabel(info).toString(), icon)
                }.getOrNull()
            }
        cache = list
        return list
    }
}

private val MESSAGING_APPS = listOf(
    "com.whatsapp", "com.whatsapp.w4b", "org.telegram.messenger", "org.thoughtcrime.securesms",
    "com.instagram.android", "com.facebook.orca", "com.google.android.apps.messaging", "com.samsung.android.messaging",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(onBack: () -> Unit, onOpen: (AppEntry) -> Unit) {
    val context = LocalContext.current
    val rules by context.hushly.rules.rules.collectAsState()
    var apps by remember { mutableStateOf(InstalledApps.cache) }
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (apps == null) apps = withContext(Dispatchers.IO) { InstalledApps.load(context) }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("App e persone") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") }
            },
        )
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("Cerca un'app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            val list = apps
            if (list == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                val shown = list
                    .filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
                    .sortedWith(
                        compareBy<AppEntry>(
                            { rules[it.packageName].let { r -> r == null || r.mode == RuleMode.OFF } },
                            { it.packageName !in MESSAGING_APPS },
                            { it.label.lowercase() },
                        ),
                    )
                LazyColumn {
                    items(shown, key = { it.packageName }) { app ->
                        AppRow(app, rules[app.packageName]) { onOpen(app) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppEntry, rule: AppRule?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app.icon != null) Image(app.icon, null, Modifier.size(40.dp)) else Spacer(Modifier.size(40.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(app.label, style = MaterialTheme.typography.bodyLarge)
            Text(ruleSummary(rule), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun ruleSummary(rule: AppRule?): String = when (rule?.mode) {
    null, RuleMode.OFF -> "In silenzio"
    RuleMode.ALL -> "Suonano tutte le notifiche"
    RuleMode.SOME -> when (rule.senders.size) {
        0 -> "Nessuna persona scelta: in silenzio"
        1 -> "Suona solo per: ${rule.senders.first()}"
        else -> "Suona per ${rule.senders.size} persone"
    }
}
