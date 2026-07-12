package io.github.donald_okara.precover.core.annotations

import io.github.donald_okara.precover.core.models.RuleType

/**
 * Declares the expected preview scenarios that must be covered for a `@Composable` component.
 *
 * Precover's [RuleType.SCENARIO_COVERAGE] rule will verify that each scenario listed here
 * is satisfied by at least one preview associated with this component.
 *
 * ### Usage
 * ```kotlin
 * @RequiresPreviewScenarios(PreviewScenario.LOADING, PreviewScenario.SUCCESS, "MyCustomState")
 * @Composable
 * fun MyComponent(...) { ... }
 * ```
 *
 * @property values An array of scenario names that are required for this component.
 * @see Scenario
 * @see PrecoverLink.scenario
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RequiresPreviewScenarios(
    vararg val values: String,
)
