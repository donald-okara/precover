package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * Validates that a component is previewed with different font scales to ensure accessibility.
 *
 * This rule checks if there are at least two different font scales used across all
 * previews for a component.
 */
class FontScaleRule : PrecoverRule {
    override val name: String = "Font Scale Coverage"
    override val weight: RuleWeight = RuleWeight.MEDIUM

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        if (composable.previews.isEmpty()) return emptyList()

        val fontScales = composable.previews.mapNotNull { it.fontScale }.toSet()
        
        return if (fontScales.size < 2) {
            listOf(RuleViolation(name, "Should be previewed with at least 2 different font scales (currently: ${fontScales.size})", Severity.INFO))
        } else {
            emptyList()
        }
    }
}
