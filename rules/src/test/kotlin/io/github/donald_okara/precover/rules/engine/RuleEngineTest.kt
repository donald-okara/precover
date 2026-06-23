package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.PreviewMetadata
import io.github.donald_okara.precover.core.models.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    @Test
    fun `test engine calculates score correctly with weights`() {
        val engine = RuleEngine()
        val metadata = listOf(
            ComposableMetadata(
                packageName = "com.example",
                fileName = "Example.kt",
                functionName = "MyComponent",
                isInternal = false,
                parameters = emptyList(),
                previews = listOf(
                    PreviewMetadata(name = "Light", uiMode = 0x10) // Light Mode only
                )
            )
        )

        val report = engine.analyze(metadata)
        
        assertEquals(1, report.components.size)
        
        // weights:
        // NoPreview: MANDATORY (5) -> Passed (100% of 5)
        // Theme: HIGH (3) -> WARNING (50% of 3 = 1.5)
        // FontScale: MEDIUM (2) -> INFO (90% of 2 = 1.8)
        // ScreenSize: MEDIUM (2) -> INFO (90% of 2 = 1.8)
        // Total points: 5 + 1.5 + 1.8 + 1.8 = 10.1
        // Total weight: 5 + 3 + 2 + 2 = 12
        // Score: 10.1 / 12 * 100 = 84.16%
        
        assertEquals(84.16f, report.components[0].score, 0.1f)
    }

    @Test
    fun `test engine gives 0 score for mandatory error`() {
        val engine = RuleEngine()
        val metadata = listOf(
            ComposableMetadata(
                packageName = "com.example",
                fileName = "Example.kt",
                functionName = "MyComponent",
                isInternal = false,
                parameters = emptyList(),
                previews = emptyList() // Fails NoPreviewRule (MANDATORY)
            )
        )

        val report = engine.analyze(metadata)
        
        assertEquals(1, report.components.size)
        assertEquals(0f, report.components[0].score, 0.1f)
    }
}
