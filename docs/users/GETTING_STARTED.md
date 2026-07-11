# Getting Started with Precover

Precover is a static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality across themes, font scales, and screen sizes.

## 1. Setup Version Catalog

The recommended way to use Precover is via a version catalog. Add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
precover = "0.1.0" # Use the latest version

[libraries]
precover-core = { group = "io.github.donald-okara", name = "core", version.ref = "precover" }
precover-ksp = { group = "io.github.donald-okara", name = "ksp", version.ref = "precover" }

[plugins]
precover-root = { id = "io.github.donald-okara.precover.root", version.ref = "precover" }
precover = { id = "io.github.donald-okara.precover", version.ref = "precover" }
```

## 2. Apply the Plugin

### Multi-Module and Single-Module Projects (Recommended)
Apply the root plugin in your root-level `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.precover.root)
}

precoverRoot {
    // Minimum aggregate coverage threshold for the whole project
    aggregateCoverageThreshold.set(80f)
}
```

The root plugin will automatically:
- Detect all Android Application and Library modules.
- Apply the KSP plugin and Precover analysis plugin to them.
- Add the necessary `core` and `ksp` dependencies.
- Aggregate all results into a single project-wide dashboard.

### Manual Application (Optional)
If you want more control, you can apply the analysis plugin manually to specific modules:

```kotlin
plugins {
    id("com.google.devtools.ksp")
    alias(libs.plugins.precover)
}

dependencies {
    implementation(libs.precover.core)
    ksp(libs.precover.ksp)
}
```

## 3. Add Dependencies (Optional)
The root plugin **automatically** adds the necessary Precover KSP processor and core library to all your Android modules. You don't need to add them manually!

However, if you have non-Android Kotlin modules or want to use a specific version, you can still add them:

```kotlin
// module-level build.gradle.kts
dependencies {
    implementation(libs.precover.core)
    ksp(libs.precover.ksp)
}
```

## 4. Configure Precover
Customize the behavior using the `precover` extension in your module-level build scripts, or globally in the root script.

### Using the DSL (Recommended)
You don't need to import any Precover classes to configure rules. Use the built-in helper methods:

```kotlin
precover {
    // Threshold for precoverCheck task (0-100)
    coverageThreshold.set(80f)
    // Maximum allowed ratio of excluded components (0.0 to 1.0)
    maxExcludedRatio.set(0.2f)
    // Enable/Disable report formats
    htmlReportEnabled.set(true)
    jsonReportEnabled.set(true)

    // Configure rules using DSL labels
    PREVIEW_PRESENCE {
        enable()
        mandatory()
    }

    THEME_COVERAGE {
        disable()
    }

    FONT_SCALE_COVERAGE {
        enable()
        medium() // Options: mandatory(), high(), medium(), low()
    }
}
```

### Global Configuration
For multi-module projects, you can configure all modules from your root `build.gradle.kts` using the `subprojects` block inside `precoverRoot`:

```kotlin
precoverRoot {
    aggregateCoverageThreshold.set(85f)

    subprojects {
        coverageThreshold.set(80f)
        PREVIEW_PRESENCE {
            mandatory()
        }
    }
}
```

## 5. Available Tasks

### Project Level (Root)
- `precoverAggregateReport`: Generates a project-wide coverage dashboard in `build/reports/precover/aggregate/`.
- `precoverAggregateCheck`: Verifies overall project coverage against the aggregate threshold.

### Module Level
- `precoverReport`: Generates coverage reports for a specific module.
- `precoverCheck`: Verifies coverage for a specific module against its threshold. This is automatically linked to the standard `check` task.
