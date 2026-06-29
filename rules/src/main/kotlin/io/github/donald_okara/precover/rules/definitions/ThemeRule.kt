package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * Validates that a component is previewed in both Light and Dark modes.
 *
 * This rule looks for the `uiMode` parameter in `@Preview` annotations.
 * It expects at least one preview to be in Light mode and one in Dark mode.
 */
class ThemeRule : PrecoverRule {
    override val type: RuleType = RuleType.THEME_COVERAGE
    override val weight: RuleWeight = RuleWeight.HIGH

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        if (composable.previews.isEmpty()) return emptyList()

        val hasLight = composable.previews.any { preview ->
            val uiMode = preview.uiMode
            uiMode == null || (uiMode and 0x30) == 0x10 || uiMode == 0
        }
        val hasDark = composable.previews.any { preview ->
            val uiMode = preview.uiMode
            uiMode != null && (uiMode and 0x30) == 0x20
        }

        val violations = mutableListOf<RuleViolation>()
        if (!hasLight) {
            violations.add(RuleViolation(name, "Missing Light Mode preview", Severity.WARNING))
        }
        if (!hasDark) {
            violations.add(RuleViolation(name, "Missing Dark Mode preview", Severity.WARNING))
        }
        return violations
    }
}
