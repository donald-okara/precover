package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule

class ScreenSizeRule : PrecoverRule {
    override val name: String = "Screen Size Coverage"

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        val devices = composable.previews.mapNotNull { it.device }.toSet()
        
        val violations = mutableListOf<RuleViolation>()
        if (devices.isEmpty() && composable.previews.none { it.widthDp != null || it.heightDp != null }) {
            violations.add(RuleViolation(name, "Missing explicit screen size or device preview", Severity.INFO))
        }
        return violations
    }
}
