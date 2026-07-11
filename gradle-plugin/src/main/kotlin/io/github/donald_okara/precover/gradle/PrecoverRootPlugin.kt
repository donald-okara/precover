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
            val androidPluginHandler = { _: Any ->
                // Automatically apply the base plugin if not already present
                if (!subproject.plugins.hasPlugin("io.github.donald-okara.precover")) {
                    subproject.plugins.apply("io.github.donald-okara.precover")
                }

                // Automatically apply KSP if not already present
                if (!subproject.plugins.hasPlugin("com.google.devtools.ksp")) {
                    subproject.plugins.apply("com.google.devtools.ksp")
                }

                // Add dependencies
                val version = rootProject.version.toString()

                val coreProject = rootProject.findProject(":core")
                if (coreProject != null) {
                    subproject.dependencies.add("implementation", coreProject)
                } else {
                    subproject.dependencies.add("implementation", "io.github.donald-okara:core:$version")
                }

                val kspProject = rootProject.findProject(":ksp")
                if (kspProject != null) {
                    subproject.dependencies.add("ksp", kspProject)
                } else {
                    subproject.dependencies.add("ksp", "io.github.donald-okara:ksp:$version")
                }
                Unit
            }

            subproject.plugins.withId("com.android.application", androidPluginHandler)
            subproject.plugins.withId("com.android.library", androidPluginHandler)

            // Apply global configuration from precoverRoot.subprojects
            subproject.pluginManager.withPlugin("io.github.donald-okara.precover") {
                val subExtension = subproject.extensions.getByType(PrecoverExtension::class.java)
                extension.getSubprojectsAction()?.execute(subExtension)

                val reportTaskProvider = subproject.tasks.named("precoverReport", PrecoverReportTask::class.java)

                reportTaskProvider.configure { reportTask ->
                    // Force JSON output for aggregation to work
                    reportTask.jsonEnabled.set(true)
                }

                aggregateReportTask.configure {
                    it.dependsOn(reportTaskProvider)
                    it.inputReports.from(reportTaskProvider.flatMap { it.outputDirectory.file("precover-report.json") })
                }

                aggregateCheckTask.configure {
                    it.dependsOn(reportTaskProvider)
                    it.inputReports.from(reportTaskProvider.flatMap { it.outputDirectory.file("precover-report.json") })
                }
            }
        }

        // Link root check to subproject checks if desired, or just standalone
        rootProject.tasks.matching { it.name == "check" }.configureEach {
            it.dependsOn(aggregateCheckTask)
        }
    }
}
