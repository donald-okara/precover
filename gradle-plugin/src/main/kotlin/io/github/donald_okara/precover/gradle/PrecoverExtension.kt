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
 * Configuration options for the Precover analysis plugin in a specific module.
 *
 * This extension is available in the `build.gradle.kts` of each module where Precover is applied:
 * ```kotlin
 * precover {
 *     coverageThreshold.set(90f)
 *     htmlReportEnabled.set(true)
 *     
 *     rules {
 *         THEME_COVERAGE {
 *             high()
 *         }
 *     }
 * }
 * ```
 */
abstract class PrecoverExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Minimum coverage percentage threshold (0-100) for this module.
     * If the calculated coverage falls below this, the `precoverCheck` task will fail.
     */
    abstract val coverageThreshold: Property<Float>

    /**
     * Path to the file used to store and read baseline coverage scores.
     * Baselines allow you to prevent coverage regression without forcing an immediate high threshold.
     */
    abstract val baselineFile: RegularFileProperty

    /**
     * Whether to use the recorded [baselineFile] as the success criterion instead of [coverageThreshold].
     * If true, `precoverCheck` will fail if the current score is lower than the baseline.
     */
    abstract val useBaseline: Property<Boolean>

    /**
     * Maximum allowed ratio of excluded components (using `@PrecoverNoPreviewRequired`)
     * relative to the total number of components (0.0 to 1.0).
     */
    abstract val maxExcludedRatio: Property<Float>

    /**
     * Whether to generate an interactive HTML report for this module.
     * Reports are typically found in `build/reports/precover/`.
     */
    abstract val htmlReportEnabled: Property<Boolean>

    /**
     * Whether to generate a machine-readable JSON report for this module.
     * Required for root-level aggregation.
     */
    abstract val jsonReportEnabled: Property<Boolean>

    /**
     * Container for configuring individual rules.
     */
    val rules: NamedDomainObjectContainer<RuleConfig> = objects.domainObjectContainer(RuleConfig::class.java)

    /**
     * Access to the rules container for advanced configuration.
     */
    fun rules(action: Action<NamedDomainObjectContainer<RuleConfig>>) {
        action.execute(rules)
    }

    /**
     * Kotlin-friendly DSL for configuring rules.
     */
    fun rules(action: NamedDomainObjectContainer<RuleConfig>.() -> Unit) {
        action(rules)
    }

    /**
     * Configures a specific rule identified by its [RuleType].
     */
    fun rule(type: RuleType, action: Action<RuleConfig>) {
        rules.named(type.name, action)
    }

    /**
     * Kotlin-friendly DSL to configure a specific rule identified by its [RuleType].
     */
    fun rule(type: RuleType, action: RuleConfig.() -> Unit) {
        rules.named(type.name) { action(it) }
    }

    // DSL Labels for all available rules
    
    /** Configures the 'Preview Presence' rule. */
    fun PREVIEW_PRESENCE(action: Action<RuleConfig>) = rule(RuleType.PREVIEW_PRESENCE, action)
    /** Configures the 'Preview Presence' rule. */
    fun PREVIEW_PRESENCE(action: RuleConfig.() -> Unit) = rule(RuleType.PREVIEW_PRESENCE, action)

    /** Configures the 'PrecoverLink Usage' rule. */
    fun PRECOVER_LINK_USAGE(action: Action<RuleConfig>) = rule(RuleType.PRECOVER_LINK_USAGE, action)
    /** Configures the 'PrecoverLink Usage' rule. */
    fun PRECOVER_LINK_USAGE(action: RuleConfig.() -> Unit) = rule(RuleType.PRECOVER_LINK_USAGE, action)

    /** Configures the 'Theme Coverage' rule (Light/Dark mode). */
    fun THEME_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.THEME_COVERAGE, action)
    /** Configures the 'Theme Coverage' rule (Light/Dark mode). */
    fun THEME_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.THEME_COVERAGE, action)

    /** Configures the 'Font Scale Coverage' rule. */
    fun FONT_SCALE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.FONT_SCALE_COVERAGE, action)
    /** Configures the 'Font Scale Coverage' rule. */
    fun FONT_SCALE_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.FONT_SCALE_COVERAGE, action)

    /** Configures the 'Screen Size Coverage' rule. */
    fun SCREEN_SIZE_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.SCREEN_SIZE_COVERAGE, action)
    /** Configures the 'Screen Size Coverage' rule. */
    fun SCREEN_SIZE_COVERAGE(action: RuleConfig.() -> Unit) = rule(RuleType.SCREEN_SIZE_COVERAGE, action)

    /** Configures the 'Scenario Coverage' rule. */
    fun SCENARIO_COVERAGE(action: Action<RuleConfig>) = rule(RuleType.SCENARIO_COVERAGE, action)
    /** Configures the 'Scenario Coverage' rule. */
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
