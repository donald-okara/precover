package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.BaselineData
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
/**
 * Task that verifies if the current coverage meets the defined requirements.
 *
 * It checks the generated report against either:
 * 1. A static [threshold].
 * 2. A recorded [baselineFile] (if [useBaseline] is true).
 *
 * It also verifies the [maxExcludedRatio] to prevent excessive use of `@PrecoverNoPreviewRequired`.
 */
abstract class PrecoverCheckTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @get:Input
    abstract val threshold: Property<Float>

    @get:Input
    abstract val maxExcludedRatio: Property<Float>

    @get:Input
    abstract val ruleOverrides: MapProperty<RuleType, RuleOverride>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineFile: RegularFileProperty

    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val useBaseline: Property<Boolean>

    @TaskAction
    fun run() {
        val json = Json { ignoreUnknownKeys = true }
        val metadataContent = metadataFile.get().asFile.readText()
        val metadata = json.decodeFromString(ListSerializer(ComposableMetadata.serializer()), metadataContent)

        val engine = RuleEngine(overrides = ruleOverrides.get())
        val report = engine.analyze(metadata)

        val currentScore = report.overallScore
        val targetThreshold = threshold.get()

        val totalComponents = report.components.count { it.isComponent }
        val excludedComponents = report.components.count { it.isComponent && it.isExcluded }
        val currentExcludedRatio = if (totalComponents > 0) excludedComponents.toFloat() / totalComponents else 0f
        val maxRatio = maxExcludedRatio.get()

        val baselineScore = if (useBaseline.get()) getBaselineScore() else null

        if (currentScore < targetThreshold) {
            if (baselineScore != null && currentScore >= baselineScore) {
                logger.lifecycle("Precover: Coverage score (${"%.1f".format(currentScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%) but meets baseline (${"%.1f".format(baselineScore)}%).")
            } else {
                val message = if (baselineScore != null) {
                    "Precover: Coverage score (${"%.1f".format(currentScore)}%) is below both threshold (${"%.1f".format(targetThreshold)}%) and baseline (${"%.1f".format(baselineScore)}%)"
                } else if (useBaseline.get()) {
                    "Precover: Coverage score (${"%.1f".format(currentScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%) and no baseline found"
                } else {
                    "Precover: Coverage score (${"%.1f".format(currentScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%)"
                }
                throw GradleException(message)
            }
        }

        if (currentExcludedRatio > maxRatio) {
            throw GradleException("Precover: Excluded components ratio (${"%.1f".format(currentExcludedRatio * 100)}%) exceeds maximum allowed (${"%.1f".format(maxRatio * 100)}%)")
        }

        logger.lifecycle("Precover: Coverage check passed! (Score: ${"%.1f".format(currentScore)}% >= ${if (baselineScore != null && currentScore < targetThreshold) "baseline %.1f".format(baselineScore) else "threshold %.1f".format(targetThreshold)}%, Excluded: ${"%.1f".format(currentExcludedRatio * 100)}% <= ${"%.1f".format(maxRatio * 100)}%)")
    }

    private fun getBaselineScore(): Float? {
        val file = baselineFile.orNull?.asFile ?: return null
        if (!file.exists()) return null

        return try {
            val json = Json { ignoreUnknownKeys = true }
            val data = json.decodeFromString(BaselineData.serializer(), file.readText())
            data.baselines[modulePath.get()]?.lastOrNull()?.score
        } catch (e: Exception) {
            logger.warn("Precover: Failed to parse baseline file: ${e.message}")
            null
        }
    }
}
