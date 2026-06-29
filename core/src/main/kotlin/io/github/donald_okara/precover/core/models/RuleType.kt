package io.github.donald_okara.precover.core.models

import java.io.Serializable

/**
 * Defines the types of coverage rules available in Precover.
 * Use these constants when configuring rules in Gradle.
 */
enum class RuleType(val displayName: String) : Serializable {
    PREVIEW_PRESENCE("Preview Presence"),
    PRECOVER_LINK_USAGE("PrecoverLink Usage"),
    THEME_COVERAGE("Theme Coverage"),
    FONT_SCALE_COVERAGE("Font Scale Coverage"),
    SCREEN_SIZE_COVERAGE("Screen Size Coverage"),
}
