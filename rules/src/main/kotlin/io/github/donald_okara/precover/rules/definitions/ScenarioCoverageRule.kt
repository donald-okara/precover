package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * Validates that all required scenarios declared via @RequiresPreviewScenarios
 * are covered by at least one preview.
 */
class ScenarioCoverageRule : PrecoverRule {
    override val type: RuleType = RuleType.SCENARIO_COVERAGE
    override val weight: RuleWeight = RuleWeight.HIGH

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        if (composable.requiredScenarios.isEmpty() || composable.ignoreAllScenarios) return emptyList()

        val coveredScenarios = composable.previews.mapNotNull { it.scenario }.toSet()
        val missingScenarios = composable.requiredScenarios
            .filter { it !in composable.ignoreScenarios }
            .filter { it !in coveredScenarios }

        return missingScenarios.map { scenario ->
            RuleViolation(
                ruleName = name,
                message = "Missing preview for required scenario: '$scenario'.",
                severity = Severity.ERROR, // Missing explicit requirement is treated as error
            )
        }
    }
}
