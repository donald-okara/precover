package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.BaselineData
import io.github.donald_okara.precover.core.models.CoverageReport
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

/**
 * Task that verifies project-wide aggregate coverage against a threshold or baseline.
 *
 * It calculates the unweighted average of all module coverage scores and checks if
 * it meets the requirements defined in `precoverRoot`.
 */
@DisableCachingByDefault(because = "Check tasks with no outputs are not cacheable")
abstract class PrecoverAggregateCheckTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputReports: ConfigurableFileCollection

    @get:Input
    abstract val threshold: Property<Float>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineFile: RegularFileProperty

    @get:Input
    abstract val useBaseline: Property<Boolean>

    @TaskAction
    fun run() {
        val json = Json { ignoreUnknownKeys = true }
        val scores = inputReports.files.mapNotNull { file ->
            try {
                val reportContent = file.readText()
                val report = json.decodeFromString(CoverageReport.serializer(), reportContent)
                if (report.components.none { it.isComponent }) null else report.overallScore
            } catch (e: Exception) {
                logger.warn("Precover: Failed to parse report ${file.path}: ${e.message}")
                null
            }
        }

        if (scores.isEmpty()) {
            if (inputReports.files.isEmpty()) {
                throw GradleException("Precover: No module reports found to check. Ensure Precover is applied and tasks are executed.")
            } else {
                throw GradleException("Precover: All found reports failed to parse. Check logs for details.")
            }
        }

        val aggregateScore = scores.average().toFloat()
        val targetThreshold = threshold.get()

        val baselineScore = if (useBaseline.get()) getBaselineScore() else null

        if (aggregateScore < targetThreshold) {
            if (baselineScore != null && aggregateScore >= baselineScore) {
                logger.lifecycle("Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%) but meets baseline (${"%.1f".format(baselineScore)}%).")
            } else {
                val message = if (baselineScore != null) {
                    "Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is below both threshold (${"%.1f".format(targetThreshold)}%) and baseline (${"%.1f".format(baselineScore)}%)"
                } else if (useBaseline.get()) {
                    "Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%) and no aggregate baseline found"
                } else {
                    "Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%)"
                }
                throw GradleException(message)
            }
        } else {
            logger.lifecycle("Precover: Project aggregate check passed! (${"%.1f".format(aggregateScore)}% >= ${"%.1f".format(targetThreshold)}%)")
        }
    }

    private fun getBaselineScore(): Float? {
        val file = baselineFile.orNull?.asFile ?: return null
        if (!file.exists()) return null

        return try {
            val json = Json { ignoreUnknownKeys = true }
            val data = json.decodeFromString(BaselineData.serializer(), file.readText())
            data.baselines[":aggregate"]?.lastOrNull()?.score
        } catch (e: Exception) {
            logger.warn("Precover: Failed to parse baseline file: ${e.message}")
            null
        }
    }
}
