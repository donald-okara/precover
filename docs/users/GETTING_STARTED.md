# Getting Started with Precover

Precover is a static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality across themes, font scales, and screen sizes.

## 1. Apply the Plugin

### For Multi-Module Projects (Recommended)
The easiest way to use Precover in a repository with multiple modules is to apply the root plugin in your root-level `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.donald-okara.precover.root") version "1.0.0"
}

precoverRoot {
    // Minimum aggregate coverage threshold for the whole project
    aggregateCoverageThreshold.set(80f)
}
```

The root plugin will automatically:
- Detect all Android Application and Library modules.
- Apply the Precover analysis plugin to them.
- Aggregate all results into a single project-wide dashboard.

### For Single Module Projects
Apply the plugin in your module-level `build.gradle.kts` (usually `:app`):

```kotlin
plugins {
    id("com.google.devtools.ksp")
    id("io.github.donald-okara.precover")
}
```

## 2. Add KSP Dependency
Regardless of how you apply the plugin, you must add the Precover KSP processor to the dependencies of any module containing Composables:

```kotlin
dependencies {
    "ksp"(project(":ksp")) // Replace with "io.github.donald-okara:precover-ksp:1.0.0" when published
}
```

## 3. Configure Precover
Customize the behavior using the `precover` extension:

```kotlin
precover {
    // Threshold for precoverCheck task (0-100)
    coverageThreshold.set(80f)
    // Enable/Disable report formats
    htmlReportEnabled.set(true)
    jsonReportEnabled.set(true)

    // Configure rules using DSL labels
    THEME_COVERAGE {
        enabled.set(true)
        weight.set(io.github.donald_okara.precover.rules.engine.RuleWeight.HIGH)
    }
}
```

## 4. Available Tasks

### Project Level (Root)
- `precoverAggregateReport`: Generates a project-wide coverage dashboard in `build/reports/precover/aggregate/`.
- `precoverAggregateCheck`: Verifies overall project coverage against the aggregate threshold.

### Module Level
- `precoverReport`: Generates coverage reports for a specific module.
- `precoverCheck`: Verifies coverage for a specific module against its threshold. This is automatically linked to the standard `check` task.
