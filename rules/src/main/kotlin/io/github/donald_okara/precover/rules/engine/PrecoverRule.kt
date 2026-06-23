package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation

import java.io.Serializable

enum class RuleWeight(val value: Int) : Serializable {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    MANDATORY(5)
}

data class RuleOverride(
    val enabled: Boolean = true,
    val weight: RuleWeight? = null
) : Serializable

interface PrecoverRule {
    val name: String
    val weight: RuleWeight get() = RuleWeight.MEDIUM
    fun evaluate(composable: ComposableMetadata): List<RuleViolation>
}
