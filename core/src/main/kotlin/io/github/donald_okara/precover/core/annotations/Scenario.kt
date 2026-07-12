package io.github.donald_okara.precover.core.annotations

/**
 * Maps a `@Preview` function to a specific scenario name.
 *
 * This is the primary way to satisfy the requirements defined by [RequiresPreviewScenarios].
 * Precover identifies that a required scenario is covered when a preview function
 * (or a [PrecoverLink]) is marked with this annotation and the [value] matches.
 *
 * ### Usage
 * ```kotlin
 * @Preview
 * @Scenario(PreviewScenario.LOADING)
 * @Composable
 * fun MyComponentLoadingPreview() { ... }
 * ```
 *
 * @property value The name of the scenario being previewed.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Scenario(
    val value: String,
)
