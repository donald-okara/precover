package io.github.donald_okara.precover.core.annotations

/**
 * Declares the expected preview scenarios for a `@Composable` component.
 * Precover will verify that each of these scenarios is represented in the component's previews.
 *
 * Example:
 * ```kotlin
 * @RequiresPreviewScenarios(PreviewScenario.Loading, PreviewScenario.Success, "MyCustomScenario")
 * @Composable
 * fun MyComponent(...) { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RequiresPreviewScenarios(
    vararg val values: String
)
