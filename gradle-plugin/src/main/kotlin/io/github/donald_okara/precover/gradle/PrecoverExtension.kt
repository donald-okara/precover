package io.github.donald_okara.precover.gradle

import org.gradle.api.provider.Property

interface PrecoverExtension {
    val coverageThreshold: Property<Float>
    val htmlReportEnabled: Property<Boolean>
    val jsonReportEnabled: Property<Boolean>
}
