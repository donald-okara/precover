package io.github.donald_okara.precover.core.annotations

/**
 * Excludes a `@Composable` from specific preview scenario validation.
 *
 * This is useful when a component is marked with [RequiresPreviewScenarios] but certain
 * states (like "Error" or "Empty") are intentionally omitted or handled elsewhere.
 *
 * ### Usage
 * ```kotlin
 * @PrecoverIgnoreScenarios(exclude = [PreviewScenario.ERROR])
 * @Composable
 * fun MyComponent() { ... }
 * ```
 *
 * @property exclude An array of scenario names to ignore. If this array is empty (the default),
 * **all** scenario validation is disabled for the annotated component.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PrecoverIgnoreScenarios(
    val exclude: Array<String> = [],
)
