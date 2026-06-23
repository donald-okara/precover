package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class PrecoverExtension {
    abstract val coverageThreshold: Property<Float>
    abstract val htmlReportEnabled: Property<Boolean>
    abstract val jsonReportEnabled: Property<Boolean>

    abstract val rules: NamedDomainObjectContainer<RuleConfig>
}

abstract class RuleConfig @Inject constructor(val name: String) {
    @Input
    fun getName(): String = name

    abstract val enabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val weight: Property<RuleWeight>
}
