package io.github.donald_okara.precover.core.models

import java.io.Serializable

/**
 * Defines the types of coverage rules available in Precover.
 *
 * Use these constants when configuring rule weights or enabling/disabling rules in the
 * `precover` or `precoverRoot` Gradle DSL.
 *
 * @property displayName A human-readable name for the rule, used in reports.
 */
enum class RuleType(val displayName: String) : Serializable {
    /** Checks if a Composable has any associated previews. */
    PREVIEW_PRESENCE("Preview Presence"),
    /** Checks if `@PrecoverLink` is used correctly for attribution. */
    PRECOVER_LINK_USAGE("PrecoverLink Usage"),
    /** Checks for both Light and Dark mode previews. */
    THEME_COVERAGE("Theme Coverage"),
    /** Checks if multiple font scales are tested. */
    FONT_SCALE_COVERAGE("Font Scale Coverage"),
    /** Checks if multiple screen sizes or devices are tested. */
    SCREEN_SIZE_COVERAGE("Screen Size Coverage"),
    /** Checks if all required scenarios (states) are previewed. */
    SCENARIO_COVERAGE("Scenario Coverage"),
}
