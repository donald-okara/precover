package io.github.donald_okara.precover.rules.engine

import io.github.donald_okara.precover.core.models.ComposableMetadata
import io.github.donald_okara.precover.core.models.PreviewMetadata
import io.github.donald_okara.precover.core.models.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    @Test
    fun `test engine calculates score correctly for theme violations`() {
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
        assertEquals("MyComponent", report.components[0].name)
        // Expected Violations:
        // 1. Theme: Missing Dark Mode (Warning = 15)
        // 2. Font Scale: size < 2 (Info = 5)
        // 3. Screen Size: missing explicit (Info = 5)
        // Total Penalty: 15 + 5 + 5 = 25. Score: 100 - 25 = 75
        assertEquals(75f, report.components[0].score, 0.1f)
        assertTrue(report.components[0].violations.any { it.message.contains("Missing Dark Mode") })
    }

    @Test
    fun `test engine ignores Preview functions`() {
        val engine = RuleEngine()
        val metadata = listOf(
            ComposableMetadata(
                packageName = "com.example",
                fileName = "Example.kt",
                functionName = "MyComponentPreview",
                isInternal = false,
                parameters = emptyList(),
                previews = emptyList()
            )
        )

        val report = engine.analyze(metadata)
        assertEquals(0, report.components.size)
    }
}
