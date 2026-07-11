# Multi-Module Coverage Aggregation

Precover is designed to scale with large repositories. By using the `io.github.donald-okara.precover.root` plugin, you can manage UI quality across dozens of modules from a single point.

## How it Works

The Root Plugin (`precover.root`) performs the following actions:

1.  **Module Discovery**: It scans your project for subprojects applying `com.android.application` or `com.android.library`.
2.  **Plugin Injection**: It automatically applies the `io.github.donald-okara.precover` plugin to those modules if they don't have it yet.
3.  **Task Orchestration**: It creates a dependency graph where the root-level aggregate tasks depend on the individual module analysis tasks.
4.  **Data Aggregation**: It collects JSON metadata from each module's analysis to compute a project-wide score.

## Root Configuration

Configure global project settings and default subproject behavior in your root `build.gradle.kts`:

```kotlin
precoverRoot {
    // Fails precoverAggregateCheck if average score < threshold
    aggregateCoverageThreshold.set(85f)
    
    // Global report settings
    htmlReportEnabled.set(true)
    jsonReportEnabled.set(true)

    // Configure all Precover-enabled subprojects at once
    subprojects {
        coverageThreshold.set(80f)
        
        PREVIEW_PRESENCE {
            enable()
            mandatory()
        }
    }
}
```

## Per-Module Overrides

Even when using the root plugin, you can still customize settings for specific modules by adding a `precover` block in that module's `build.gradle.kts`:

```kotlin
// feature/login/build.gradle.kts
precover {
    // This module requires higher coverage
    coverageThreshold.set(95f)
    
    THEME_COVERAGE {
        enabled.set(false) // Disable theme checks just for this module
    }
}
```

The aggregator will respect these overrides when calculating the module's contribution to the project total.

## Aggregated Reports

The `precoverAggregateReport` task generates a consolidated HTML dashboard. This dashboard provides:
- **Total Project Score**: The weighted average of all analyzed modules.
- **Module Comparison**: A list of all modules with their individual scores and component counts.
- **Deep Links**: Clicking on a module name takes you directly to that module's detailed Precover report.

## CI Integration

To enforce coverage across your entire repository, add `precoverAggregateCheck` to your CI pipeline, or simply run `./gradlew check`, as the aggregate check is automatically linked to the root `check` task.
