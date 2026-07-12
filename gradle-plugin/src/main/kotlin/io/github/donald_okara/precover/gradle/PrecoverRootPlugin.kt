package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import java.util.Properties

/**
 * The root plugin for Precover that orchestrates aggregate reporting and global configuration.
 *
 * This plugin should be applied to the root project of a multi-module repository.
 *
 * It provides:
 * - The `precoverRoot` extension for project-wide settings.
 * - Automatic application of [PrecoverPlugin] and KSP to all Android subprojects.
 * - Aggregate reporting tasks that combine results from all modules into a single dashboard.
 */
class PrecoverRootPlugin : Plugin<Project> {
    /**
     * Applies the root plugin to the specified [rootProject].
     */
    override fun apply(rootProject: Project) {
        if (rootProject != rootProject.rootProject) {
            rootProject.logger.warn("Precover: The root plugin should only be applied to the root project.")
            return
        }

        val extension = rootProject.extensions.create("precoverRoot", PrecoverRootExtension::class.java)
        extension.aggregateCoverageThreshold.convention(80f)
        extension.htmlReportEnabled.convention(true)
        extension.jsonReportEnabled.convention(true)
        extension.baselineFile.convention(rootProject.layout.projectDirectory.file("precover/baselines.json"))
        extension.useBaseline.convention(true)

        val aggregateReportTask = rootProject.tasks.register("precoverAggregateReport", PrecoverAggregateReportTask::class.java) {
            it.group = "verification"
            it.description = "Aggregates Precover reports from all modules"
            it.outputDirectory.set(rootProject.layout.buildDirectory.dir("reports/precover/aggregate"))
            it.htmlEnabled.set(extension.htmlReportEnabled)
            it.jsonEnabled.set(extension.jsonReportEnabled)
        }

        val aggregateCheckTask = rootProject.tasks.register("precoverAggregateCheck", PrecoverAggregateCheckTask::class.java) {
            it.group = "verification"
            it.description = "Checks aggregate Precover coverage against threshold or baseline"
            it.threshold.set(extension.aggregateCoverageThreshold)
            it.baselineFile.set(extension.baselineFile)
            it.useBaseline.set(extension.useBaseline)
        }

        val aggregateUpdateBaselineTask = rootProject.tasks.register("precoverAggregateUpdateBaseline", PrecoverAggregateUpdateBaselineTask::class.java) {
            it.group = "verification"
            it.description = "Updates the project aggregate Precover coverage baseline"
            it.baselineFile.set(extension.baselineFile)
        }

        val updateBaselineTask = rootProject.tasks.register("precoverUpdateBaseline") {
            it.group = "verification"
            it.description = "Updates Precover coverage baselines for all modules and the project aggregate"
            it.dependsOn(aggregateUpdateBaselineTask)
        }

        rootProject.subprojects { subproject ->
            val androidPluginHandler = { _: Any ->
                // Automatically apply the base plugin if not already present
                if (!subproject.plugins.hasPlugin("io.github.donald-okara.precover.module")) {
                    subproject.plugins.apply("io.github.donald-okara.precover.module")
                }

                // Automatically apply KSP if not already present
                if (!subproject.plugins.hasPlugin("com.google.devtools.ksp")) {
                    subproject.plugins.apply("com.google.devtools.ksp")
                }

                // Add dependencies
                val version = getPrecoverVersion(rootProject)

                val coreProject = rootProject.findProject(":core")?.takeIf { isPrecoverProject(it) }
                if (coreProject != null) {
                    subproject.dependencies.add("implementation", coreProject)
                } else {
                    subproject.dependencies.add("implementation", "io.github.donald-okara.precover:core:$version")
                }

                val kspProject = rootProject.findProject(":ksp")?.takeIf { isPrecoverProject(it) }
                if (kspProject != null) {
                    subproject.dependencies.add("ksp", kspProject)
                } else {
                    subproject.dependencies.add("ksp", "io.github.donald-okara.precover:ksp:$version")
                }
                Unit
            }

            subproject.plugins.withId("com.android.application", androidPluginHandler)
            subproject.plugins.withId("com.android.library", androidPluginHandler)

            // Apply global configuration from precoverRoot.subprojects
            subproject.pluginManager.withPlugin("io.github.donald-okara.precover.module") {
                val subExtension = subproject.extensions.getByType(PrecoverExtension::class.java)
                extension.getSubprojectsAction()?.execute(subExtension)

                // Root baseline file should be the default for subprojects if not explicitly set
                subExtension.baselineFile.convention(extension.baselineFile)
                subExtension.useBaseline.convention(extension.useBaseline)

                val reportTaskProvider = subproject.tasks.named("precoverReport", PrecoverReportTask::class.java)
                val updateBaselineTaskProvider = subproject.tasks.named("precoverUpdateBaseline", PrecoverUpdateBaselineTask::class.java)

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

                aggregateUpdateBaselineTask.configure {
                    it.dependsOn(reportTaskProvider)
                    it.inputReports.from(reportTaskProvider.flatMap { it.outputDirectory.file("precover-report.json") })
                }

                updateBaselineTask.configure {
                    it.dependsOn(updateBaselineTaskProvider)
                }
            }
        }

        // Link root check to subproject checks if desired, or just standalone
        rootProject.tasks.matching { it.name == "check" }.configureEach {
            it.dependsOn(aggregateCheckTask)
        }
    }

    private fun getPrecoverVersion(project: Project): String {
        val properties = Properties()
        val resource = javaClass.classLoader.getResourceAsStream("precover-version.properties")
        resource?.use {
            properties.load(it)
            return properties.getProperty("version") ?: project.version.toString()
        }
        return project.version.toString()
    }

    private fun isPrecoverProject(project: Project): Boolean = project.group == "io.github.donald-okara.precover" ||
        project.hasProperty("isPrecoverModule")
}
