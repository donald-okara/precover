package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule

class FontScaleRule : PrecoverRule {
    override val name: String = "Font Scale Coverage"

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        val fontScales = composable.previews.mapNotNull { it.fontScale }.toSet()
        
        val violations = mutableListOf<RuleViolation>()
        if (fontScales.size < 2) {
            violations.add(RuleViolation(name, "Should be previewed with at least 2 different font scales (currently: ${fontScales.size})", Severity.INFO))
        }
        return violations
    }
}
