package io.github.donald_okara.precover.gradle

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration options for the Precover root plugin.
 */
abstract class PrecoverRootExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Minimum aggregate coverage threshold for the entire project.
     * If total coverage falls below this, precoverAggregateCheck will fail.
     */
    abstract val aggregateCoverageThreshold: Property<Float>

    /**
     * File to store and read baseline coverage scores.
     */
    abstract val baselineFile: RegularFileProperty

    /**
     * Whether to use recorded baselines as an alternative to the hard threshold.
     */
    abstract val useBaseline: Property<Boolean>

    /**
     * Whether to generate an aggregated HTML report at the root level.
     */
    abstract val htmlReportEnabled: Property<Boolean>

    /**
     * Whether to generate an aggregated JSON report at the root level.
     */
    abstract val jsonReportEnabled: Property<Boolean>

    private val subprojectsAction = objects.property(Action::class.java as Class<Action<PrecoverExtension>>)

    /**
     * Configure all Precover-enabled subprojects from the root.
     */
    fun subprojects(action: Action<PrecoverExtension>) {
        subprojectsAction.set(action)
    }

    /**
     * Kotlin-friendly version of [subprojects].
     */
    fun subprojects(action: PrecoverExtension.() -> Unit) {
        subprojects(Action { it.action() })
    }

    internal fun getSubprojectsAction(): Action<PrecoverExtension>? = subprojectsAction.orNull as Action<PrecoverExtension>?
}
