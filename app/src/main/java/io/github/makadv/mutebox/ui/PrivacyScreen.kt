package io.github.makadv.mutebox.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Informativa sulla privacy mostrata prima di chiedere l'accesso alle notifiche (artt. 12-13 GDPR e
 * "prominent disclosure" richiesta da Google Play per i permessi sensibili).
 *
 * @param onAccept null quando l'informativa è già stata accettata: allora la schermata è di sola lettura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onAccept: (() -> Unit)?, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Privacy e dati") },
            navigationIcon = {
                if (onAccept == null) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") }
                }
            },
        )
    }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "MuteBox funziona interamente sul tuo telefono. Prima di concedere l'accesso alle notifiche, " +
                    "leggi cosa fa con i tuoi dati.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Section(
                "Cosa legge",
                "Con il permesso \"Accesso alle notifiche\" Android permette a MuteBox di vedere le notifiche in arrivo. " +
                    "MuteBox le guarda solo per le app a cui hai dato una regola (\"Tutte\" o \"Solo alcune persone\"): " +
                    "le notifiche delle altre app, come banca o salute, vengono ignorate subito. " +
                    "Dalle notifiche legge mittente e nome della conversazione per decidere se far suonare il telefono. " +
                    "Il testo del messaggio non viene mai salvato: ne resta in memoria solo un'impronta numerica " +
                    "per evitare suoni doppi.",
            )
            Section(
                "Cosa salva",
                "Solo nella memoria privata dell'app:\n" +
                    "• le tue impostazioni e le regole per app e persone;\n" +
                    "• fino a 40 nomi di mittenti visti di recente, solo per le app in modalità \"Solo alcune persone\", " +
                    "per suggerirteli. Vengono cancellati in automatico dopo 30 giorni.",
            )
            Section(
                "Dove vanno i dati",
                "Da nessuna parte. MuteBox non ha il permesso di accesso a Internet, non contiene pubblicità, " +
                    "analytics o tracciamento ed esclude i propri dati dal backup cloud. " +
                    "Lo sviluppatore non riceve né vede alcun dato.",
            )
            Section(
                "I tuoi diritti",
                "Dalla schermata principale, nella sezione \"Privacy e dati\", puoi:\n" +
                    "• esportare tutti i dati salvati in un file JSON;\n" +
                    "• cancellare i nomi visti o tutti i dati;\n" +
                    "• revocare in qualsiasi momento l'accesso alle notifiche dalle impostazioni di Android.\n" +
                    "Disinstallando l'app, Android cancella tutti i suoi dati.",
            )
            Text(
                "L'informativa completa è nel file PRIVACY.md del repository GitHub del progetto.",
                style = MaterialTheme.typography.bodySmall,
            )
            if (onAccept != null) {
                // Uno sotto l'altro e a tutta larghezza: affiancati, con caratteri grandi il testo va a capo.
                Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Accetto e continuo", textAlign = TextAlign.Center)
                    }
                    OutlinedButton(onClick = { (context as? Activity)?.finish() }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Esci", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
