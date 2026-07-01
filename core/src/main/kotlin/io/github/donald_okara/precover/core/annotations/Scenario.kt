package io.github.donald_okara.precover.core.annotations

/**
 * Maps a `@Preview` function to a specific scenario name.
 * This is used to satisfy the requirements defined by `@RequiresPreviewScenarios`.
 *
 * Example:
 * ```kotlin
 * @Preview
 * @Scenario(PreviewScenario.Loading)
 * @Composable
 * fun MyComponentLoadingPreview() { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Scenario(
    val value: String,
)
