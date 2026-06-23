package io.github.donald_okara.precover.core.models

import kotlinx.serialization.Serializable

@Serializable
data class CoverageReport(
    val overallScore: Float,
    val components: List<ComponentCoverage>
)

@Serializable
data class ComponentCoverage(
    val name: String,
    val packageName: String,
    val score: Float,
    val violations: List<RuleViolation>
)

@Serializable
data class RuleViolation(
    val ruleName: String,
    val message: String,
    val severity: Severity = Severity.WARNING
)

@Serializable
enum class Severity {
    INFO, WARNING, ERROR
}
