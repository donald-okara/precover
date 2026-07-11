package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class PrecoverExtension @Inject constructor(objects: ObjectFactory) {
    abstract val coverageThreshold: Property<Float>
    abstract val maxExcludedRatio: Property<Float>
    abstract val htmlReportEnabled: Property<Boolean>
    abstract val jsonReportEnabled: Property<Boolean>

    val rules: NamedDomainObjectContainer<RuleConfig> = objects.domainObjectContainer(RuleConfig::class.java)

    /**
     * Access to the rules container for more advanced configuration.
     */
    fun rules(action: Action<NamedDomainObjectContainer<RuleConfig>>) {
        action.execute(rules)
    }

    /**
     * Kotlin-friendly access to the rules container.
     */
    fun rules(action: NamedDomainObjectContainer<RuleConfig>.() -> Unit) {
        action(rules)
    }

    /**
     * Convenience method to configure a rule using its [RuleType] enum.
     */
    fun rule(type: RuleType, action: Action<RuleConfig>) {
        rules.named(type.name, action)
    }

    /**
     * Kotlin-friendly convenience method to configure a rule using its [RuleType] enum.
     */
    fun rule(type: RuleType, action: RuleConfig.() -> Unit) {
        rules.named(type.name) { action(it) }
    }

    // DSL Labels for all available rules
    fun PREVIEW_PRESENCE(action: Action<RuleConfig>) = rule(RuleType.PREVIEW_PRESENCE, action)
    fun PREVIEW_PRESENCE(action: RuleConfig.() -> Unit) = rule(RuleType.PREVIEW_PRESENCE, action)

    fun PRECOVER_LINK_USAGE(action: Action<RuleConfig>) = rule(RuleType.PRECOVER_LINK_USAGE, action)
    fun PRECOVER_LINK_USAGE(action: RuleConfig.() -> Unit) = rule(RuleType.PRECOVER_LINK_USAGE, action)

    fun THEME_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.THEME_COVERAGE, action)
    fun THEME_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.THEME_COVERAGE, action)

    fun FONT_SCALE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.FONT_SCALE_COVERAGE, action)
    fun FONT_SCALE_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.FONT_SCALE_COVERAGE, action)

    fun SCREEN_SIZE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.SCREEN_SIZE_COVERAGE, action)
    fun SCREEN_SIZE_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.SCREEN_SIZE_COVERAGE, action)

    fun SCENARIO_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.SCENARIO_COVERAGE, action)
    fun SCENARIO_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.SCENARIO_COVERAGE, action)

    // Standard lowercase versions
    fun previewPresence(action: Action<RuleConfig>) = PREVIEW_PRESENCE(action)
    fun previewPresence(action: RuleConfig.() -> Unit) = PREVIEW_PRESENCE(action)

    fun precoverLinkUsage(action: Action<RuleConfig>) = PRECOVER_LINK_USAGE(action)
    fun precoverLinkUsage(action: RuleConfig.() -> Unit) = PRECOVER_LINK_USAGE(action)

    fun themeCoverage(action: Action<RuleConfig>) = THEME_COVERAGE(action)
    fun themeCoverage(action: RuleConfig.() -> Unit) = THEME_COVERAGE(action)

    fun fontScaleCoverage(action: Action<RuleConfig>) = FONT_SCALE_COVERAGE(action)
    fun fontScaleCoverage(action: RuleConfig.() -> Unit) = FONT_SCALE_COVERAGE(action)

    fun screenSizeCoverage(action: Action<RuleConfig>) = SCREEN_SIZE_COVERAGE(action)
    fun screenSizeCoverage(action: RuleConfig.() -> Unit) = SCREEN_SIZE_COVERAGE(action)

    fun scenarioCoverage(action: Action<RuleConfig>) = SCENARIO_COVERAGE(action)
    fun scenarioCoverage(action: RuleConfig.() -> Unit) = SCENARIO_COVERAGE(action)
}

abstract class RuleConfig @Inject constructor(private val ruleName: String) {

    @Internal
    fun getName(): String = ruleName

    @get:Internal
    val type: RuleType by lazy { RuleType.valueOf(ruleName) }

    @get:Input
    abstract val enabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val weight: Property<RuleWeight>

    fun mandatory() = weight.set(RuleWeight.MANDATORY)
    fun high() = weight.set(RuleWeight.HIGH)
    fun medium() = weight.set(RuleWeight.MEDIUM)
    fun low() = weight.set(RuleWeight.LOW)

    fun enable() = enabled.set(true)
    fun disable() = enabled.set(false)
}
