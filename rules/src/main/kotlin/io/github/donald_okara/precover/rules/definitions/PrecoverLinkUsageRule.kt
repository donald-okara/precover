package io.github.donald_okara.precover.rules.definitions

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation
import io.github.donald_okara.precover.core.models.Severity
import io.github.donald_okara.precover.rules.engine.PrecoverRule
import io.github.donald_okara.precover.rules.engine.RuleWeight

/**
 * Validates that @PrecoverLink is only used on functions that also have @Preview.
 *
 * @PrecoverLink is an accessory and does not provide a preview by itself.
 * It is intended to be used alongside standard @Preview annotations to link
 * them to a target component.
 */
class PrecoverLinkUsageRule : PrecoverRule {
    override val name: String = "PrecoverLink Usage"
    override val weight: RuleWeight = RuleWeight.MANDATORY

    override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
        val hasPrecoverLink = composable.annotations.any { 
            it == "io.github.donald_okara.precover.core.annotations.PrecoverLink" 
        }

        if (hasPrecoverLink && !composable.hasDirectPreviews) {
            return listOf(
                RuleViolation(
                    ruleName = name,
                    message = "Function '${composable.functionName}' has @PrecoverLink but no direct @Preview annotation. @PrecoverLink is an accessory and requires a @Preview on the same function to be effective.",
                    severity = Severity.ERROR
                )
            )
        }
        return emptyList()
    }
}
