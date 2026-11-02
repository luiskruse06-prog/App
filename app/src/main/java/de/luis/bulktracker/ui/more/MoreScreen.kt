package de.luis.bulktracker.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private data class MoreEntry(val title: String, val hint: String, val icon: ImageVector)

private val entries = listOf(
    MoreEntry("Schlaf", "kommt in Phase 4", Icons.Filled.Bedtime),
    MoreEntry("Körpergewicht", "kommt in Phase 3", Icons.Filled.MonitorWeight),
    MoreEntry("Einstellungen", "kommt mit Phase 2", Icons.Filled.Settings),
    MoreEntry("Backup (JSON)", "kommt in Phase 6", Icons.Filled.Save),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen() {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text("Mehr") }) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            entries.forEach { entry ->
                ListItem(
                    headlineContent = { Text(entry.title) },
                    supportingContent = { Text(entry.hint) },
                    leadingContent = { Icon(entry.icon, contentDescription = null) },
                )
            }
        }
    }
}
