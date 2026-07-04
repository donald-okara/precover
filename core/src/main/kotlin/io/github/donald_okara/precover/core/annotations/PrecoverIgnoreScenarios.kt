package io.github.donald_okara.precover.core.annotations

/**
 * Excludes a `@Composable` from specific preview scenario validation.
 *
 * Example:
 * ```kotlin
 * @PrecoverIgnoreScenarios(exclude = [PreviewScenario.LOADING])
 * @Composable
 * fun MyComponent() { ... }
 * ```
 *
 * If `exclude` is empty (default), all scenarios are ignored for this component.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PrecoverIgnoreScenarios(
    val exclude: Array<String> = []
)
