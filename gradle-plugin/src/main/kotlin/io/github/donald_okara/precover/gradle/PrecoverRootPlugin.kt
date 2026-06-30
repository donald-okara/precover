package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.gradle.tasks.PrecoverAggregateCheckTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverAggregateReportTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class PrecoverRootPlugin : Plugin<Project> {
    override fun apply(rootProject: Project) {
        if (rootProject != rootProject.rootProject) {
            rootProject.logger.warn("Precover: The root plugin should only be applied to the root project.")
            return
        }

        val extension = rootProject.extensions.create("precoverRoot", PrecoverRootExtension::class.java)
        extension.aggregateCoverageThreshold.convention(80f)
        extension.htmlReportEnabled.convention(true)
        extension.jsonReportEnabled.convention(true)

        val aggregateReportTask = rootProject.tasks.register("precoverAggregateReport", PrecoverAggregateReportTask::class.java) {
            it.group = "verification"
            it.description = "Aggregates Precover reports from all modules"
            it.outputDirectory.set(rootProject.layout.buildDirectory.dir("reports/precover/aggregate"))
            it.htmlEnabled.set(extension.htmlReportEnabled)
            it.jsonEnabled.set(extension.jsonReportEnabled)
        }

        val aggregateCheckTask = rootProject.tasks.register("precoverAggregateCheck", PrecoverAggregateCheckTask::class.java) {
            it.group = "verification"
            it.description = "Checks aggregate Precover coverage against threshold"
            it.threshold.set(extension.aggregateCoverageThreshold)
        }

        rootProject.subprojects { subproject ->
            rootProject.logger.info("Precover: Considering subproject ${subproject.path}")

            // Use withId to avoid afterEvaluate where possible
            val androidPluginHandler = { _: Any ->
                rootProject.logger.info("Precover: Subproject ${subproject.path} is an Android project")
                // Automatically apply the base plugin if not already present
                if (!subproject.plugins.hasPlugin("io.github.donald-okara.precover")) {
                    subproject.plugins.apply("io.github.donald-okara.precover")
                }

                // Link submodule report to root aggregation lazily
                subproject.tasks.all { task ->
                    if (task.name == "precoverReport") {
                        val reportTask = task as PrecoverReportTask
                        aggregateReportTask.configure {
                            it.dependsOn(reportTask)
                            it.inputReports.from(reportTask.outputDirectory.file("precover-report.json"))
                        }

                        aggregateCheckTask.configure {
                            it.dependsOn(reportTask)
                            it.inputReports.from(reportTask.outputDirectory.file("precover-report.json"))
                        }
                    }
                }
            }

            subproject.plugins.withId("com.android.application", androidPluginHandler)
            subproject.plugins.withId("com.android.library", androidPluginHandler)
        }

        // Link root check to subproject checks if desired, or just standalone
        rootProject.tasks.matching { it.name == "check" }.configureEach {
            it.dependsOn(aggregateCheckTask)
        }
    }
}
