package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Configuration options for the Precover analysis plugin.
 */
abstract class PrecoverExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Minimum coverage threshold (0-100) for this module.
     */
    abstract val coverageThreshold: Property<Float>

    /**
     * File to store and read baseline coverage scores.
     */
    abstract val baselineFile: RegularFileProperty

    /**
     * Whether to use recorded baselines as an alternative to the hard threshold.
     */
    abstract val useBaseline: Property<Boolean>

    /**
     * Maximum allowed ratio of excluded components (0.0 to 1.0).
     */
    abstract val maxExcludedRatio: Property<Float>

    /**
     * Whether to generate an HTML report for this module.
     */
    abstract val htmlReportEnabled: Property<Boolean>

    /**
     * Whether to generate a JSON report for this module.
     */
    abstract val jsonReportEnabled: Property<Boolean>

    /**
     * Container for rule-specific configurations.
     */
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

/**
 * Configuration for a specific Precover rule.
 */
abstract class RuleConfig @Inject constructor(private val ruleName: String) {

    /**
     * Returns the name of the rule.
     */
    @Internal
    fun getName(): String = ruleName

    /**
     * Returns the [RuleType] of this rule.
     */
    @get:Internal
    val type: RuleType by lazy { RuleType.valueOf(ruleName) }

    /**
     * Whether this rule is enabled.
     */
    @get:Input
    abstract val enabled: Property<Boolean>

    /**
     * The importance weight of this rule.
     */
    @get:Input
    @get:Optional
    abstract val weight: Property<RuleWeight>

    /**
     * Sets the rule weight to [RuleWeight.MANDATORY].
     */
    fun mandatory() = weight.set(RuleWeight.MANDATORY)

    /**
     * Sets the rule weight to [RuleWeight.HIGH].
     */
    fun high() = weight.set(RuleWeight.HIGH)

    /**
     * Sets the rule weight to [RuleWeight.MEDIUM].
     */
    fun medium() = weight.set(RuleWeight.MEDIUM)

    /**
     * Sets the rule weight to [RuleWeight.LOW].
     */
    fun low() = weight.set(RuleWeight.LOW)

    /**
     * Enables the rule.
     */
    fun enable() = enabled.set(true)

    /**
     * Disables the rule.
     */
    fun disable() = enabled.set(false)
}
