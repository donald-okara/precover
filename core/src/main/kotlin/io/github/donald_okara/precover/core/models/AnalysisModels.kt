package io.github.donald_okara.precover.core.models

import kotlinx.serialization.Serializable

/**
 * A detailed coverage report for a single module.
 *
 * @property overallScore The weighted average coverage score (0-100) for the module.
 * @property components List of coverage results for each analyzed component in the module.
 * @property modulePath The Gradle path of the module (e.g., ":app").
 */
@Serializable
data class CoverageReport(
    val overallScore: Float,
    val components: List<ComponentCoverage>,
    val modulePath: String? = null,
)

/**
 * Coverage analysis results for a single `@Composable` component.
 *
 * @property name The name of the component function.
 * @property packageName The package containing the component.
 * @property score The individual coverage score (0-100) for this component.
 * @property violations List of rule violations found for this component.
 * @property passedRules List of rule names that this component satisfied.
 * @property requiredScenarios Scenarios that were required but potentially not covered.
 * @property coveredScenarios Scenarios that were successfully previewed.
 * @property isComponent Whether this is treated as a top-level component.
 * @property isExcluded Whether this component was excluded via `@PrecoverNoPreviewRequired`.
 */
@Serializable
data class ComponentCoverage(
    val name: String,
    val packageName: String,
    val score: Float,
    val violations: List<RuleViolation>,
    val passedRules: List<String> = emptyList(),
    val requiredScenarios: List<String> = emptyList(),
    val coveredScenarios: List<String> = emptyList(),
    val isComponent: Boolean = true,
    val isExcluded: Boolean = false,
)

/**
 * Represents a failure to satisfy a specific Precover rule.
 *
 * @property ruleName The human-readable name of the rule (e.g., "Theme Coverage").
 * @property message A detailed message explaining the violation and how to fix it.
 * @property severity The importance of the violation.
 */
@Serializable
data class RuleViolation(
    val ruleName: String,
    val message: String,
    val severity: Severity = Severity.WARNING,
)

/**
 * The severity level of a [RuleViolation].
 */
@Serializable
enum class Severity {
    /** A suggestion for improvement with no impact on the pass/fail status. */
    INFO,
    /** A recommended check that might affect the score but won't fail the build unless threshold is high. */
    WARNING,
    /** A critical failure that usually drops the component score to 0 or fails mandatory rules. */
    ERROR,
}

/**
 * An aggregate coverage report for a multi-module project.
 *
 * @property overallScore The weighted average coverage score across all modules.
 * @property modules Summary of coverage for each individual module.
 */
@Serializable
data class AggregateCoverageReport(
    val overallScore: Float,
    val modules: List<ModuleCoverage>,
)

/**
 * Summary coverage data for a single module in an aggregate report.
 */
@Serializable
data class ModuleCoverage(
    /** The Gradle path of the module. */
    val modulePath: String,
    /** The overall coverage score of the module. */
    val score: Float,
    /** The number of analyzed components in the module. */
    val componentCount: Int,
    /** Path to the module-specific report file. */
    val reportPath: String? = null,
)

/**
 * Container for baseline coverage data.
 *
 * Baselines are used to track coverage over time and prevent regressions.
 */
@Serializable
data class BaselineData(
    /** Map of module paths or component names to their history of baseline entries. */
    val baselines: Map<String, List<BaselineEntry>> = emptyMap(),
)

/**
 * A single recorded coverage score at a point in time.
 */
@Serializable
data class BaselineEntry(
    /** The recorded coverage score. */
    val score: Float,
    /** The time the baseline was recorded (milliseconds since epoch). */
    val timestamp: Long,
)
