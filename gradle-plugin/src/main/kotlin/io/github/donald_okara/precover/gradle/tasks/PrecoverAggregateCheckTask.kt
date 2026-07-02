package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.CoverageReport
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Check tasks with no outputs are not cacheable")
abstract class PrecoverAggregateCheckTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputReports: ConfigurableFileCollection

    @get:Input
    abstract val threshold: Property<Float>

    @TaskAction
    fun run() {
        val json = Json { ignoreUnknownKeys = true }
        val scores = inputReports.files.mapNotNull { file ->
            try {
                val reportContent = file.readText()
                json.decodeFromString(CoverageReport.serializer(), reportContent).overallScore
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

        if (aggregateScore < targetThreshold) {
            throw GradleException("Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is below threshold (${"%.1f".format(targetThreshold)}%)")
        } else {
            logger.lifecycle("Precover: Project aggregate check passed! (${"%.1f".format(aggregateScore)}% >= ${"%.1f".format(targetThreshold)}%)")
        }
    }
}
