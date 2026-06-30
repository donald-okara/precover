package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * Validates that a component is previewed on different screen sizes or devices.
 *
 * This rule looks for explicit `device`, `widthDp`, or `heightDp` parameters
 * in the preview metadata to ensure adaptive layouts are being verified.
 */
class ScreenSizeRule : PrecoverRule {
    override val type: RuleType = RuleType.SCREEN_SIZE_COVERAGE
    override val weight: RuleWeight = RuleWeight.MEDIUM

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        if (composable.previews.isEmpty()) return emptyList()

        val hasExplicitSize = composable.previews.any {
            it.device != null || it.widthDp != null || it.heightDp != null
        }

        return if (!hasExplicitSize) {
            listOf(RuleViolation(name, "Missing explicit screen size or device preview", Severity.INFO))
        } else {
            emptyList()
        }
    }
}
