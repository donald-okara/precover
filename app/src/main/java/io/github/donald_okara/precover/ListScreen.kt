package io.github.donald_okara.precover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "List - Light")
@Preview(showBackground = true, name = "List - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ListScreen(onItemClick: (Int) -> Unit = {}, onSettingsClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Precover Items") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items((1..20).toList()) { id ->
                ListItem(
                    headlineContent = { Text("Item $id") },
                    supportingContent = { Text("Description for item $id") },
                    modifier = Modifier.clickable { onItemClick(id) },
                )
            }
        }
    }
}
