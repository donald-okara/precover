package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

class NoPreviewRule : PrecoverRule {
    override val name: String = "Preview Presence"
    override val weight: RuleWeight = RuleWeight.MANDATORY

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        return if (composable.previews.isEmpty()) {
            listOf(RuleViolation(name, "Composable has no @Preview annotations", Severity.ERROR))
        } else {
            emptyList()
        }
    }
}
