package io.github.donald_okara.precover.core.annotations

/**
 * Indicates that a `@Composable` does not require any previews for coverage analysis.
 *
 * This should be used for:
 * - Low-level UI primitives that are tested as part of larger components.
 * - Internal helper composables.
 * - Debug-only UI elements.
 * - Wrappers that don't add visual logic.
 *
 * Components marked with this annotation will still appear in Precover reports but will
 * be marked as "Excluded" and will not negatively impact the overall coverage score.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PrecoverNoPreviewRequired
