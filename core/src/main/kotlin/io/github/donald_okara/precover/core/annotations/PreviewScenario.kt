package io.github.donald_okara.precover.core.annotations

/**
 * Standard scenario names for convenience.
 *
 * These constants represent common UI states. While these are recommended for consistency,
 * you can use any arbitrary string as a scenario name in Precover.
 */
object PreviewScenario {
    /** The component is in a loading state. */
    const val LOADING = "Loading"

    /** The component is displaying an error message. */
    const val ERROR = "Error"

    /** The component is displaying its primary content successfully. */
    const val SUCCESS = "Success"

    /** The component is in an empty or "no data" state. */
    const val EMPTY = "Empty"

    /** The component is in a disabled or non-interactive state. */
    const val DISABLED = "Disabled"

    /** The component is in a read-only state. */
    const val READ_ONLY = "ReadOnly"
}
