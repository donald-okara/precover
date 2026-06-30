package io.github.donald_okara.precover.core

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

/**
 * Enhanced `PreviewParameterProvider` that supports scenario metadata.
 * Use the `scenario` helper to wrap your preview values.
 *
 * NOTE: KSP support for this is currently experimental.
 */
abstract class PrecoverPreviewParameterProvider<T> : PreviewParameterProvider<T> {
    // This is mostly a marker class for now.
    // The KSP processor will look for usages of 'scenario(String, T)' inside the provider.
}

/**
 * Internal marker function to associate a value with a scenario.
 * The KSP processor parses calls to this function to derive coverage.
 */
fun <T> scenario(name: String, value: T): T = value
