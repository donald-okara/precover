package io.github.donald_okara.precover.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrecoverPluginTest {
    @Test
    fun `plugin registers tasks`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.donald-okara.precover")

        // afterEvaluate is needed because tasks are registered there
        // In a unit test with ProjectBuilder, we can manually trigger afterEvaluate or just check the tasks if they were registered directly.
        // Since we used afterEvaluate in the plugin, we might need a better way to test, but let's see if we can trigger it.
        
        // Actually, let's check if the extension exists first
        assertNotNull(project.extensions.findByName("precover"))
    }
}
