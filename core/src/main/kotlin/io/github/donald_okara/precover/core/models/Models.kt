package io.github.donald_okara.precover.core.models

import kotlinx.serialization.Serializable

/**
 * Raw metadata for a `@Composable` function extracted during KSP processing.
 *
 * This data class contains all the information needed to evaluate coverage rules,
 * including associated previews, parameters, and exclusion annotations.
 *
 * @property packageName The package containing the composable.
 * @property fileName The source file name.
 * @property functionName The name of the composable function.
 * @property isInternal Whether the function has the `internal` modifier.
 * @property parameters Metadata for each parameter of the function.
 * @property previews List of previews directly or indirectly associated with this function.
 * @property linkTargets Names of other composables that this function links to (via @PrecoverLink).
 * @property requiredScenarios List of scenario names required for this component.
 * @property ignoreScenarios List of scenario names explicitly ignored for this component.
 * @property ignoreAllScenarios Whether all scenario validation is disabled.
 * @property noPreviewRequired Whether this component is excluded from coverage analysis.
 * @property annotations List of simple names of all annotations on the function.
 * @property isComponent Whether this function is considered a top-level UI component.
 * @property hasDirectPreviews Whether this function has @Preview annotations directly applied to it.
 */
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
    val ignoreScenarios: List<String> = emptyList(),
    val ignoreAllScenarios: Boolean = false,
    val noPreviewRequired: Boolean = false,
    val annotations: List<String> = emptyList(),
    val isComponent: Boolean = true,
    val hasDirectPreviews: Boolean = false,
)

/**
 * Metadata for a parameter of a `@Composable` function.
 */
@Serializable
data class ParameterMetadata(
    /** The name of the parameter. */
    val name: String,
    /** The fully qualified name or simple name of the parameter type. */
    val type: String,
    /** Whether the parameter has a default value defined. */
    val hasDefaultValue: Boolean,
    /** Metadata if this parameter is annotated with `@PreviewParameter`. */
    val previewParameter: PreviewParameterMetadata? = null,
)

/**
 * Metadata for a parameter annotated with `@PreviewParameter`.
 */
@Serializable
data class PreviewParameterMetadata(
    /** The fully qualified name of the provider class. */
    val providerType: String,
    /** The limit parameter passed to the annotation, if any. */
    val limit: Int? = null,
    /** List of scenario names derived from the provider (if it's a `PrecoverPreviewParameterProvider`). */
    val scenarios: List<String> = emptyList(),
)

/**
 * Metadata for a single preview associated with a component.
 *
 * This includes parameters extracted from the `@Preview` annotation or from a `@PrecoverLink`.
 */
@Serializable
data class PreviewMetadata(
    /** The `name` parameter of the `@Preview`. */
    val name: String? = null,
    /** The `group` parameter of the `@Preview`. */
    val group: String? = null,
    /** The `apiLevel` parameter of the `@Preview`. */
    val apiLevel: Int? = null,
    /** The `widthDp` parameter of the `@Preview`. */
    val widthDp: Int? = null,
    /** The `heightDp` parameter of the `@Preview`. */
    val heightDp: Int? = null,
    /** The `locale` parameter of the `@Preview`. */
    val locale: String? = null,
    /** The `fontScale` parameter of the `@Preview`. */
    val fontScale: Float? = null,
    /** The `showBackground` parameter of the `@Preview`. */
    val showBackground: Boolean? = null,
    /** The `backgroundColor` parameter of the `@Preview`. */
    val backgroundColor: Long? = null,
    /** The `showSystemUi` parameter of the `@Preview`. */
    val showSystemUi: Boolean? = null,
    /** The `device` parameter of the `@Preview`. */
    val device: String? = null,
    /** The `uiMode` parameter of the `@Preview`. */
    val uiMode: Int? = null,
    /** The `wallpaper` parameter of the `@Preview`. */
    val wallpaper: Int? = null,
    /** Whether this metadata was derived from a `@PrecoverLink` instead of a direct `@Preview`. */
    val isLink: Boolean = false,
    /** The scenario name associated with this preview. */
    val scenario: String? = null,
)
