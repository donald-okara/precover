package io.github.donald_okara.precover.core.annotations

import kotlin.reflect.KClass

/**
 * Explicitly links a preview function to one or more `@Composable` targets to explain coverage.
 *
 * This annotation is an "accessory" used to provide explicit attribution and metadata
 * to the Precover engine. It does not provide a preview itself (use the standard `@Preview`
 * alongside it for IDE support).
 *
 * ### Usage
 *
 * Use this to "explain" coverage for complex scenarios or when naming conventions are not enough.
 * ```kotlin
 * @Preview(name = "Success State")
 * @PrecoverLink("DetailScreen", name = "Success Scenario")
 * @Composable
 * fun MyComplexPreview() { ... }
 * ```
 *
 * @param value The name of the target `@Composable` function.
 * @param target A `KClass` for type-safe linkage (e.g., a NavKey or Marker).
 * @param name The semantic name of the scenario/state for Precover reports.
 * @param group Categorization group.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Repeatable
annotation class PrecoverLink(
    val value: String = "",
    val target: KClass<*> = Unit::class,
    val name: String = "",
    val group: String = "",
)
