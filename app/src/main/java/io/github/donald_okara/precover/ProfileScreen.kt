package io.github.donald_okara.precover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.donald_okara.precover.core.PrecoverPreviewParameterProvider
import io.github.donald_okara.precover.core.annotations.PreviewScenario
import io.github.donald_okara.precover.core.annotations.RequiresPreviewScenarios
import io.github.donald_okara.precover.core.annotations.Scenario
import io.github.donald_okara.precover.core.scenario
import io.github.donald_okara.precover.ui.theme.PrecoverTheme

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@RequiresPreviewScenarios(
    PreviewScenario.LOADING,
    PreviewScenario.EMPTY,
    PreviewScenario.ERROR,
    PreviewScenario.SUCCESS,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()

                state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)

                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text(state.name, style = MaterialTheme.typography.headlineMedium)
                    Text(state.email, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// --- Demo 1: Explicit Scenario Annotation ---

@Preview(showBackground = true, name = "Profile - Loading")
@Scenario(PreviewScenario.LOADING)
@Composable
fun ProfileLoadingPreview() {
    PrecoverTheme {
        ProfileScreen(state = ProfileUiState(isLoading = true))
    }
}

// --- Demo 2: Provider-based Scenarios (Recommended) ---

class ProfileStateProvider : PrecoverPreviewParameterProvider<ProfileUiState>() {
    override val values = sequenceOf(
        scenario(
            PreviewScenario.SUCCESS,
            ProfileUiState(name = "Don Okara", email = "don@example.com"),
        ),
        scenario(
            PreviewScenario.ERROR,
            ProfileUiState(error = "Failed to load profile"),
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileProviderPreview(
    @PreviewParameter(ProfileStateProvider::class) state: ProfileUiState,
) {
    PrecoverTheme {
        ProfileScreen(state = state)
    }
}
