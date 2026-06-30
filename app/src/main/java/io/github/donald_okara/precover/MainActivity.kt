package io.github.donald_okara.precover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.donald_okara.precover.ui.theme.PrecoverTheme
import kotlinx.serialization.Serializable

@Serializable
data object ItemList : NavKey

@Serializable
data class ItemDetail(val id: Int, val title: String) : NavKey

@Serializable
data object Settings : NavKey

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrecoverTheme {
                PrecoverApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240", name = "App - Tablet")
@Composable
fun PrecoverApp() {
    val backStack = rememberNavBackStack(ItemList)
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val directive = calculatePaneScaffoldDirective(adaptiveInfo)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        sceneStrategy = listDetailStrategy,
        entryProvider = entryProvider {
            entry<ItemList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select an item to see details")
                        }
                    },
                ),
            ) {
                ListScreen(
                    onItemClick = { id -> backStack.add(ItemDetail(id, "Item $id")) },
                    onSettingsClick = { backStack.add(Settings) },
                )
            }
            entry<ItemDetail>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { item ->
                DetailScreen(item = item)
            }
            entry<Settings>(
                metadata = ListDetailSceneStrategy.extraPane(),
            ) {
                SettingsScreen()
            }
        },
    )
}
