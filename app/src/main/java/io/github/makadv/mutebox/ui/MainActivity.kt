package io.github.makadv.mutebox.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuteBoxTheme { MuteBoxNav() }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data object Apps : Screen
    data class Detail(val packageName: String, val label: String) : Screen
}

@Composable
private fun MuteBoxNav() {
    var stack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val back: () -> Unit = { stack = stack.dropLast(1) }
    BackHandler(enabled = stack.size > 1, onBack = back)
    when (val screen = stack.last()) {
        Screen.Home -> HomeScreen(onOpenApps = { stack = stack + Screen.Apps })
        Screen.Apps -> AppsScreen(
            onBack = back,
            onOpen = { app -> stack = stack + Screen.Detail(app.packageName, app.label) },
        )
        is Screen.Detail -> AppDetailScreen(screen.packageName, screen.label, onBack = back)
    }
}
