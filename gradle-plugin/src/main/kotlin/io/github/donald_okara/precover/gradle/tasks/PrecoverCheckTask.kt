package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.rules.engine.RuleEngine
import io.github.donald_okara.precover.rules.engine.RuleOverride
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Check tasks with no outputs are not cacheable")
abstract class PrecoverCheckTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @get:Input
    abstract val threshold: Property<Float>

    @get:Input
    abstract val ruleOverrides: MapProperty<RuleType, RuleOverride>

    @TaskAction
    fun run() {
        val json = Json { ignoreUnknownKeys = true }
        val metadataContent = metadataFile.get().asFile.readText()
        val metadata = json.decodeFromString(ListSerializer(ComposableMetadata.serializer()), metadataContent)

        val engine = RuleEngine(overrides = ruleOverrides.get())
        val report = engine.analyze(metadata)

        val currentScore = report.overallScore
        val targetThreshold = threshold.get()

        if (currentScore < targetThreshold) {
            throw GradleException("Precover: Coverage score (${"%.1f".format(currentScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%)")
        } else {
            logger.lifecycle("Precover: Coverage check passed! (${"%.1f".format(currentScore)}% >= ${"%.1f".format(targetThreshold)}%)")
        }
    }
}
