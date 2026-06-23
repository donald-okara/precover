package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.gradle.tasks.PrecoverCheckTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class PrecoverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("precover", PrecoverExtension::class.java).apply {
            coverageThreshold.convention(80f)
            htmlReportEnabled.convention(true)
            jsonReportEnabled.convention(true)
        }

        project.afterEvaluate {
            // Find the KSP task and metadata file for the debug variant
            // In a real plugin, we would iterate over variants, but for this task we target debug
            val kspTaskName = "kspDebugKotlin"
            val kspTask = project.tasks.findByName(kspTaskName)
            
            val metadataFile = project.file("build/generated/ksp/debug/resources/io/github/donald_okara/precover/precover-metadata.json")

            val reportTask = project.tasks.register("precoverReport", PrecoverReportTask::class.java) {
                it.group = "verification"
                it.description = "Generates Precover coverage reports"
                it.metadataFile.set(metadataFile)
                it.outputDirectory.set(project.layout.buildDirectory.dir("reports/precover"))
                it.htmlEnabled.set(extension.htmlReportEnabled)
                it.jsonEnabled.set(extension.jsonReportEnabled)
                if (kspTask != null) it.dependsOn(kspTask)
            }

            val checkTask = project.tasks.register("precoverCheck", PrecoverCheckTask::class.java) {
                it.group = "verification"
                it.description = "Checks Precover coverage against threshold"
                it.metadataFile.set(metadataFile)
                it.threshold.set(extension.coverageThreshold)
                if (kspTask != null) it.dependsOn(kspTask)
            }

            // Link to the standard check task
            project.tasks.findByName("check")?.dependsOn(checkTask)
        }
    }
}
