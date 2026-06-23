package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleViolation

interface PrecoverRule {
    val name: String
    fun evaluate(composable: ComposableMetadata): List<RuleViolation>
}
