package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class PrecoverExtension {
    abstract val coverageThreshold: Property<Float>
    abstract val htmlReportEnabled: Property<Boolean>
    abstract val jsonReportEnabled: Property<Boolean>

    abstract val rules: NamedDomainObjectContainer<RuleConfig>

    /**
     * Convenience method to configure a rule using its [RuleType] enum.
     */
    fun rule(type: RuleType, action: Action<RuleConfig>) {
        rules.named(type.name, action)
    }

    // DSL Labels for all available rules
    fun PREVIEW_PRESENCE(action: Action<RuleConfig>) = rule(RuleType.PREVIEW_PRESENCE, action)
    fun PRECOVER_LINK_USAGE(action: Action<RuleConfig>) = rule(RuleType.PRECOVER_LINK_USAGE, action)
    fun THEME_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.THEME_COVERAGE, action)
    fun FONT_SCALE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.FONT_SCALE_COVERAGE, action)
    fun SCREEN_SIZE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.SCREEN_SIZE_COVERAGE, action)
}

abstract class RuleConfig @Inject constructor(private val name: String) {

    @get:Internal
    val type: RuleType by lazy { RuleType.valueOf(name) }

    @get:Input
    abstract val enabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val weight: Property<RuleWeight>
}
