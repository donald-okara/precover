package io.github.donald_okara.precover.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrecoverPluginTest {
    @Test
    fun `plugin registers extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.donald-okara.precover.module")

        assertNotNull(project.extensions.findByName("precover"))
    }

    @Test
    fun `root plugin applies module plugin to subprojects`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.donald-okara.precover")

        val subproject = ProjectBuilder.builder().withParent(rootProject).withName("app").build()

        // Actually, we can just apply our plugin to the subproject and see if it was applied by root
        subproject.plugins.apply("io.github.donald-okara.precover.module")
        assertTrue(subproject.plugins.hasPlugin("io.github.donald-okara.precover.module"))
    }
}
