package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.*
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.rules.definitions.*

class RuleEngine(
    private val rules: List<PrecoverRule> = listOf(
        NoPreviewRule(),
        PrecoverLinkUsageRule(),
        ThemeRule(),
        FontScaleRule(),
        ScreenSizeRule()
    ),
    private val overrides: Map<RuleType, RuleOverride> = emptyMap()
) {
    fun analyze(metadata: List<ComposableMetadata>): CoverageReport {
        val activeRules = rules.filter { rule ->
            overrides[rule.type]?.enabled ?: true
        }

        val componentCoverages = metadata.map { composable ->
            val applicableRules = if (composable.isComponent) {
                // Components get all rules. PrecoverLinkUsageRule now handles checking
                // if the component itself was erroneously annotated with @PrecoverLink.
                activeRules
            } else {
                // Accessories only get the link usage rule
                activeRules.filter { it is PrecoverLinkUsageRule }
            }

            val ruleViolations = applicableRules.associateWith { it.evaluate(composable) }
            
            // If any MANDATORY rule has an ERROR, score is 0
            val hasMandatoryError = applicableRules.any { rule ->
                val weight = overrides[rule.type]?.weight ?: rule.weight
                weight == RuleWeight.MANDATORY && 
                ruleViolations[rule]?.any { it.severity == Severity.ERROR } == true
            }

            // Also treat empty previews as a hard 0 regardless of enabled rules
            // (Only for components - accessories are checked by PrecoverLinkUsageRule)
            val score = if (hasMandatoryError || (composable.isComponent && composable.previews.isEmpty())) {
                0f
            } else {
                var earnedPoints = 0f
                var totalWeight = 0f

                applicableRules.forEach { rule ->
                    val violations = ruleViolations[rule] ?: emptyList()
                    val weight = (overrides[rule.type]?.weight ?: rule.weight).value.toFloat()
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

                if (totalWeight > 0) (earnedPoints / totalWeight) * 100f else 100f
            }

            ComponentCoverage(
                name = composable.functionName,
                packageName = composable.packageName,
                score = score,
                violations = ruleViolations.values.flatten(),
                isComponent = composable.isComponent
            )
        }

        val overallScore = if (componentCoverages.isEmpty()) 0f else {
            val componentScores = componentCoverages.filter { it.isComponent }.map { it.score }
            if (componentScores.isEmpty()) 0f else componentScores.average().toFloat()
        }

        return CoverageReport(
            overallScore = overallScore,
            components = componentCoverages
        )
    }
}
