package io.github.donald_okara.precover.core

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

import io.github.donald_okara.precover.core.annotations.RequiresPreviewScenarios

/**
 * Enhanced [PreviewParameterProvider] that supports automated scenario coverage derivation.
 *
 * When using this provider, you can wrap your provided values with the [scenario] helper
 * function. Precover's KSP processor will detect these calls and automatically satisfy
 * [RequiresPreviewScenarios] for any component using this provider.
 *
 * ### Usage
 * ```kotlin
 * class MyProvider : PrecoverPreviewParameterProvider<MyState>() {
 *     override val values = sequenceOf(
 *         scenario(PreviewScenario.LOADING, MyState.Loading),
 *         scenario(PreviewScenario.SUCCESS, MyState.Success)
 *     )
 * }
 * ```
 *
 * @see scenario
 */
abstract class PrecoverPreviewParameterProvider<T> : PreviewParameterProvider<T> {
    // This is mostly a marker class for now.
    // The KSP processor will look for usages of 'scenario(String, T)' inside the provider.
}

/**
 * Associates a preview value with a specific scenario name for coverage tracking.
 *
 * This function is parsed by the Precover KSP processor. At runtime, it simply
 * returns the [value] unchanged.
 *
 * @param name The name of the scenario to associate with this value.
 * @param value The actual value to be used in the preview.
 * @return The provided [value].
 */
fun <T> scenario(name: String, value: T): T = value
