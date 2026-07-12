package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.BaselineData
import io.github.donald_okara.precover.core.models.BaselineEntry
import io.github.donald_okara.precover.core.models.CoverageReport
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Task that records the current coverage score as the new baseline for a module.
 *
 * It only updates the baseline if the current score exceeds the existing recorded baseline.
 */
@DisableCachingByDefault(because = "Updating baseline is an explicit action that should always run when requested")
abstract class PrecoverUpdateBaselineTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFile: RegularFileProperty

    @get:OutputFile
    abstract val baselineFile: RegularFileProperty

    @get:Input
    abstract val modulePath: Property<String>

    @TaskAction
    fun run() {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        val reportContent = reportFile.get().asFile.readText()
        val report = json.decodeFromString(CoverageReport.serializer(), reportContent)
        val currentScore = report.overallScore
        val path = modulePath.get()

        val baselineFileHandle = baselineFile.get().asFile
        val baselineData = if (baselineFileHandle.exists()) {
            try {
                json.decodeFromString(BaselineData.serializer(), baselineFileHandle.readText())
            } catch (e: Exception) {
                logger.warn("Precover: Failed to parse baseline file, creating new one. ${e.message}")
                BaselineData()
            }
        } else {
            if (!baselineFileHandle.parentFile.exists()) {
                baselineFileHandle.parentFile.mkdirs()
            }
            BaselineData()
        }

        val history = baselineData.baselines[path]?.toMutableList() ?: mutableListOf()
        val lastScore = history.lastOrNull()?.score ?: -1f

        if (currentScore > lastScore) {
            history.add(BaselineEntry(currentScore, System.currentTimeMillis()))
            val updatedBaselines = baselineData.baselines.toMutableMap()
            updatedBaselines[path] = history
            val updatedData = baselineData.copy(baselines = updatedBaselines)

            baselineFileHandle.writeText(json.encodeToString(BaselineData.serializer(), updatedData))
            logger.lifecycle("Precover: Updated baseline for $path to ${"%.1f".format(currentScore)}% (was ${if (lastScore >= 0) "%.1f".format(lastScore) else "none"}%)")
        } else if (currentScore == lastScore) {
            logger.lifecycle("Precover: Current score for $path is equal to baseline (${"%.1f".format(currentScore)}%). No update needed.")
        } else {
            logger.lifecycle("Precover: Current score for $path (${"%.1f".format(currentScore)}%) is lower than baseline (${"%.1f".format(lastScore)}%). Skipping update.")
        }
    }
}
