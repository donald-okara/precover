package io.github.donald_okara.precover.gradle

import org.gradle.api.provider.Property

abstract class PrecoverRootExtension {
    /**
     * Minimum aggregate coverage threshold for the entire project.
     * If total coverage falls below this, precoverAggregateCheck will fail.
     */
    abstract val aggregateCoverageThreshold: Property<Float>

    /**
     * Whether to generate an aggregated HTML report at the root level.
     */
    abstract val htmlReportEnabled: Property<Boolean>

    /**
     * Whether to generate an aggregated JSON report at the root level.
     */
    abstract val jsonReportEnabled: Property<Boolean>
}
