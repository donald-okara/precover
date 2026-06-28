package io.github.donald_okara.precover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.donald_okara.precover.core.annotations.PrecoverLink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { androidx.compose.material3.TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Settings Content", style = MaterialTheme.typography.titleLarge)
            Text("Configure your app here.")
        }
    }
}

@Preview(name = "Settings - Separate")
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}

@Preview(name = "Settings - Custom Name")
@Composable
fun MyCoolSettingsPreview() {
    SettingsScreen()
}

@Preview
@Composable
fun SettingsScreen_Loading() {
    SettingsScreen()
}

@Preview
@Composable
fun SettingsScreen_Error() {
    SettingsScreen()
}
