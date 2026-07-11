package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.BaselineData
import io.github.donald_okara.precover.core.models.BaselineEntry
import io.github.donald_okara.precover.core.models.CoverageReport
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Updating baseline is an explicit action that should always run when requested")
abstract class PrecoverAggregateUpdateBaselineTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputReports: ConfigurableFileCollection

    @get:OutputFile
    abstract val baselineFile: RegularFileProperty

    @TaskAction
    fun run() {
        val json = Json { 
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        val scores = inputReports.files.mapNotNull { file ->
            try {
                val reportContent = file.readText()
                json.decodeFromString(CoverageReport.serializer(), reportContent).overallScore
            } catch (e: Exception) {
                logger.warn("Precover: Failed to parse report ${file.path}: ${e.message}")
                null
            }
        }

        if (scores.isEmpty()) return

        val aggregateScore = scores.average().toFloat()
        val path = ":aggregate"

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

        if (aggregateScore > lastScore) {
            history.add(BaselineEntry(aggregateScore, System.currentTimeMillis()))
            val updatedBaselines = baselineData.baselines.toMutableMap()
            updatedBaselines[path] = history
            val updatedData = baselineData.copy(baselines = updatedBaselines)
            
            baselineFileHandle.writeText(json.encodeToString(BaselineData.serializer(), updatedData))
            logger.lifecycle("Precover: Updated aggregate baseline to ${"%.1f".format(aggregateScore)}% (was ${if (lastScore >= 0) "%.1f".format(lastScore) else "none"}%)")
        } else if (aggregateScore == lastScore) {
             logger.lifecycle("Precover: Project aggregate score is equal to baseline (${"%.1f".format(aggregateScore)}%). No update needed.")
        } else {
            logger.lifecycle("Precover: Project aggregate score (${"%.1f".format(aggregateScore)}%) is lower than baseline (${"%.1f".format(lastScore)}%). Skipping update.")
        }
    }
}
