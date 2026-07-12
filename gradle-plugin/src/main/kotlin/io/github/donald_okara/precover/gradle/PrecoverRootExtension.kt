package io.github.donald_okara.precover.gradle

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration options for the Precover root plugin.
 *
 * This extension is available in the root `build.gradle.kts` as `precoverRoot`:
 * ```kotlin
 * precoverRoot {
 *     aggregateCoverageThreshold.set(85f)
 *     
 *     subprojects {
 *         // Global configuration for all modules
 *         coverageThreshold.set(80f)
 *     }
 * }
 * ```
 */
abstract class PrecoverRootExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Minimum aggregate coverage threshold (0-100) for the entire project.
     * If the total weighted average coverage across all modules falls below this,
     * the `precoverAggregateCheck` task will fail.
     */
    abstract val aggregateCoverageThreshold: Property<Float>

    /**
     * Path to the file used to store and read aggregate baseline coverage scores.
     * Defaults to `precover/baselines.json` in the root project.
     */
    abstract val baselineFile: RegularFileProperty

    /**
     * Whether to use the recorded [baselineFile] as the aggregate success criterion
     * instead of [aggregateCoverageThreshold].
     */
    abstract val useBaseline: Property<Boolean>

    /**
     * Whether to generate an aggregated HTML report summarizing results from all modules.
     */
    abstract val htmlReportEnabled: Property<Boolean>

    /**
     * Whether to generate an aggregated JSON report for external consumption.
     */
    abstract val jsonReportEnabled: Property<Boolean>

    private val subprojectsAction = objects.property(Action::class.java as Class<Action<PrecoverExtension>>)

    /**
     * Provides a way to configure all Precover-enabled subprojects from the root.
     * The provided [action] will be applied to the [PrecoverExtension] of each subproject.
     */
    fun subprojects(action: Action<PrecoverExtension>) {
        subprojectsAction.set(action)
    }

    /**
     * Kotlin-friendly DSL version of [subprojects].
     */
    fun subprojects(action: PrecoverExtension.() -> Unit) {
        subprojects(Action { it.action() })
    }

    internal fun getSubprojectsAction(): Action<PrecoverExtension>? = subprojectsAction.orNull as Action<PrecoverExtension>?
}
