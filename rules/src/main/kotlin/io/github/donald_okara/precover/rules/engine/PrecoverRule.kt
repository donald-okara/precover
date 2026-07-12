package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.core.models.RuleViolation
import java.io.Serializable

/**
 * Defines the importance of a rule in the overall coverage score calculation.
 */
enum class RuleWeight(val value: Int) : Serializable {
    /** Minor suggestions. Low impact on the overall score. Useful for optional best practices. */
    LOW(1),

    /** Standard check. Contributes moderately to the score. The default for most rules. */
    MEDIUM(2),

    /** Important coverage requirement. Significantly impacts the score if failed. */
    HIGH(3),

    /**
     * Critical check. If a mandatory rule fails, the component's coverage score
     * will drop to 0% regardless of other rules.
     */
    MANDATORY(5),
}

/**
 * Configuration override for a rule, typically provided via the Gradle extension.
 */
data class RuleOverride(
    val enabled: Boolean = true,
    val weight: RuleWeight? = null,
) : Serializable

/**
 * Base interface for all Precover coverage rules.
 *
 * Implement this interface to add new static analysis checks for Compose Previews.
 */
interface PrecoverRule {
    /** The unique type identifier of the rule. */
    val type: RuleType

    /** The unique display name of the rule. */
    val name: String get() = type.displayName

    /** The default weight of the rule if not overridden by the user. */
    val weight: RuleWeight get() = RuleWeight.MEDIUM

    /**
     * Evaluates a single composable component against this rule.
     *
     * @param composable The metadata of the component to analyze, including its extracted previews.
     * @return A list of [RuleViolation]s found. Return an empty list if the rule passes.
     */
    fun evaluate(composable: ComposableMetadata): List<RuleViolation>
}
