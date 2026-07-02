package io.github.donald_okara.precover.core.models

import kotlinx.serialization.Serializable

@Serializable
data class ComposableMetadata(
    val packageName: String,
    val fileName: String,
    val functionName: String,
    val isInternal: Boolean,
    val parameters: List<ParameterMetadata>,
    val previews: List<PreviewMetadata>,
    val linkTargets: List<String> = emptyList(),
    val requiredScenarios: List<String> = emptyList(),
    val annotations: List<String> = emptyList(),
    val isComponent: Boolean = true,
    val hasDirectPreviews: Boolean = false,
)

@Serializable
data class ParameterMetadata(
    val name: String,
    val type: String,
    val hasDefaultValue: Boolean,
    val previewParameter: PreviewParameterMetadata? = null,
)

@Serializable
data class PreviewParameterMetadata(
    val providerType: String,
    val limit: Int? = null,
    val scenarios: List<String> = emptyList(),
)

@Serializable
data class PreviewMetadata(
    val name: String? = null,
    val group: String? = null,
    val apiLevel: Int? = null,
    val widthDp: Int? = null,
    val heightDp: Int? = null,
    val locale: String? = null,
    val fontScale: Float? = null,
    val showBackground: Boolean? = null,
    val backgroundColor: Long? = null,
    val showSystemUi: Boolean? = null,
    val device: String? = null,
    val uiMode: Int? = null,
    val wallpaper: Int? = null,
    val isLink: Boolean = false,
    val scenario: String? = null,
)
