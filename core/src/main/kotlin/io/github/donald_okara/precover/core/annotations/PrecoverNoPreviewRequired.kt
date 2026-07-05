package io.github.donald_okara.precover.core.annotations

/**
 * Indicates that a `@Composable` does not require any previews for coverage.
 * Useful for low-level UI primitives, internal helpers, or debug-only components.
 *
 * Components marked with this will still appear in reports but will be marked as "Excluded"
 * and won't contribute to the overall coverage score.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PrecoverNoPreviewRequired
