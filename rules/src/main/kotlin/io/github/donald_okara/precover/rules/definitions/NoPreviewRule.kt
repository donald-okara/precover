package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * The most fundamental rule: validates that a component has at least one preview.
 *
 * This is a MANDATORY rule. If a component has no previews, its coverage score
 * will be 0% regardless of other passing rules.
 */
class NoPreviewRule : PrecoverRule {
    override val type: RuleType = RuleType.PREVIEW_PRESENCE
    override val weight: RuleWeight = RuleWeight.MANDATORY

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        return if (composable.previews.isEmpty()) {
            listOf(RuleViolation(name, "Composable has no @Preview annotations", Severity.ERROR))
        } else {
            emptyList()
        }
    }
}
