package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.*
import io.github.donald_okara.precover.rules.definitions.*

class RuleEngine(
    private val rules: List<PrecoverRule> = listOf(
        NoPreviewRule(),
        ThemeRule(),
        FontScaleRule(),
        ScreenSizeRule()
    ),
    private val overrides: Map<String, RuleOverride> = emptyMap()
) {
    fun analyze(metadata: List<ComposableMetadata>): CoverageReport {
        val targetComposables = metadata.filter { it.functionName.endsWith("Preview").not() }

        val activeRules = rules.filter { rule ->
            overrides[rule.name]?.enabled ?: true
        }

        val componentCoverages = targetComposables.map { composable ->
            val ruleViolations = activeRules.associateWith { it.evaluate(composable) }
            
            // If any MANDATORY rule has an ERROR, score is 0
            val hasMandatoryError = activeRules.any { rule ->
                val weight = overrides[rule.name]?.weight ?: rule.weight
                weight == RuleWeight.MANDATORY && 
                ruleViolations[rule]?.any { it.severity == Severity.ERROR } == true
            }

            val score = if (hasMandatoryError) {
                0f
            } else {
                var earnedPoints = 0f
                var totalWeight = 0f

                activeRules.forEach { rule ->
                    val violations = ruleViolations[rule] ?: emptyList()
                    val weight = (overrides[rule.name]?.weight ?: rule.weight).value.toFloat()
                    totalWeight += weight

                    // Calculate how much this rule passed
                    // ERROR = 0%, WARNING = 50%, INFO = 90%, None = 100%
                    val ruleCompletion = when {
                        violations.any { it.severity == Severity.ERROR } -> 0.0f
                        violations.any { it.severity == Severity.WARNING } -> 0.5f
                        violations.any { it.severity == Severity.INFO } -> 0.9f
                        else -> 1.0f
                    }
                    
                    earnedPoints += weight * ruleCompletion
                }

                if (totalWeight > 0) (earnedPoints / totalWeight) * 100f else 0f
            }

            ComponentCoverage(
                name = composable.functionName,
                packageName = composable.packageName,
                score = score,
                violations = ruleViolations.values.flatten()
            )
        }

        val overallScore = if (componentCoverages.isEmpty()) 0f else componentCoverages.map { it.score }.average().toFloat()

        return CoverageReport(
            overallScore = overallScore,
            components = componentCoverages
        )
    }
}
