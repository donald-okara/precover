package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.*
import io.github.donald_okara.precover.rules.definitions.*

class RuleEngine(
    private val rules: List<PrecoverRule> = listOf(
        ThemeRule(),
        FontScaleRule(),
        ScreenSizeRule()
    )
) {
    fun analyze(metadata: List<ComposableMetadata>): CoverageReport {
        // Only analyze composables that are intended to be UI components (e.g. have parameters or are previewed)
        // For simplicity, we filter out composables that are purely for Preview (like GreetingPreview)
        val targetComposables = metadata.filter { it.functionName.endsWith("Preview").not() }

        val componentCoverages = targetComposables.map { composable ->
            val violations = rules.flatMap { it.evaluate(composable) }
            
            // Calculate a simple score: 100% minus a penalty for each violation
            // Weight: Warning = 15%, Info = 5%, Error = 30%
            var penalty = 0f
            violations.forEach { 
                penalty += when (it.severity) {
                    Severity.ERROR -> 30f
                    Severity.WARNING -> 15f
                    Severity.INFO -> 5f
                }
            }
            val score = (100f - penalty).coerceAtLeast(0f)

            ComponentCoverage(
                name = composable.functionName,
                packageName = composable.packageName,
                score = score,
                violations = violations
            )
        }

        val overallScore = if (componentCoverages.isEmpty()) 0f else componentCoverages.map { it.score }.average().toFloat()

        return CoverageReport(
            overallScore = overallScore,
            components = componentCoverages
        )
    }
}
