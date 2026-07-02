package io.github.donald_okara.precover.gradle.tasks

import io.github.donald_okara.precover.core.models.AggregateCoverageReport
import io.github.donald_okara.precover.core.models.CoverageReport
import io.github.donald_okara.precover.core.models.ModuleCoverage
import io.github.donald_okara.precover.rules.report.AggregateHtmlReporter
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class PrecoverAggregateReportTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputReports: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val htmlEnabled: Property<Boolean>

    @get:Input
    abstract val jsonEnabled: Property<Boolean>

    @TaskAction
    fun run() {
        val json = Json { ignoreUnknownKeys = true }
        val modules = mutableListOf<ModuleCoverage>()

        inputReports.files.forEach { file ->
            try {
                val reportContent = file.readText()
                val report = json.decodeFromString(CoverageReport.serializer(), reportContent)

                // Use module path from report if available, fallback to heuristic
                val modulePath = report.modulePath ?: file.absolutePath
                    .substringAfter("/precover/")
                    .substringBefore("/build/")
                    .replace("/", ":")
                    .let { if (it.startsWith(":")) it else ":$it" }

                modules.add(
                    ModuleCoverage(
                        modulePath = modulePath,
                        score = report.overallScore,
                        componentCount = report.components.size,
                        reportPath = "../../${file.parentFile.relativeTo(outputDirectory.get().asFile.parentFile.parentFile).path}/precover-report.html",
                    ),
                )
            } catch (e: Exception) {
                logger.warn("Precover: Failed to parse report ${file.path}: ${e.message}")
            }
        }

        if (modules.isEmpty()) {
            throw GradleException("Precover: No module reports found to aggregate. Ensure Precover is applied and tasks are executed.")
        }

        val overallScore = modules.map { it.score }.average().toFloat()
        val aggregateReport = AggregateCoverageReport(overallScore, modules)

        val outDir = outputDirectory.get().asFile
        if (!outDir.exists()) outDir.mkdirs()

        if (jsonEnabled.get()) {
            val reportJson = Json {
                prettyPrint = true
                encodeDefaults = true
            }
            val encoded = reportJson.encodeToString(AggregateCoverageReport.serializer(), aggregateReport)
            File(outDir, "aggregate-report.json").writeText(encoded)
        }

        if (htmlEnabled.get()) {
            val htmlReporter = AggregateHtmlReporter()
            val html = htmlReporter.generate(aggregateReport)
            File(outDir, "aggregate-report.html").writeText(html)
        }

        logger.lifecycle("Precover: Aggregate Analysis complete. Project score: ${"%.1f".format(overallScore)}%")
        logger.lifecycle("Precover: Aggregate reports generated in ${outDir.absolutePath}")
    }
}
