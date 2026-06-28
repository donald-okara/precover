package io.github.donald_okara.precover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import io.github.donald_okara.precover.core.annotations.PrecoverLink

@Target(AnnotationTarget.FUNCTION)
annotation class DetailScreenMarker

@DetailScreenMarker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(item: ItemDetail = ItemDetail(1, "Item 1")) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(item.title) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Details for ${item.title}", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("This is the detailed view of item ${item.id}. It is shown in the detail pane on large screens or as a full screen on mobile.")
        }
    }
}

@Preview(showBackground = true, name = "Detail - Light")
@Preview(fontScale = 1.5f, name = "Detail - Large Font")
@Composable
fun MyDetailPreview() {
    DetailScreen()
}

@Preview(
    showBackground = true, fontScale = 1.2f
)
@PrecoverLink(target = DetailScreenMarker::class , name = "Detail - Via NavKey")
@Composable
fun MyAdvancedDetailPreview() {
    DetailScreen()
}

@PrecoverLink("DetailScreen", name = "Detail - Via Marker")
@Preview(name = "Detail - Via Marker")
@Composable
fun MyDetailByNamePreview() {
    DetailScreen()
}
