# Getting Started with Precover

Precover is a static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality across themes, font scales, and screen sizes.

## 1. Apply the Plugin
Add the Precover plugin to your project-level `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.donald-okara.precover") version "1.0.0" apply false
}
```

Then apply it in your module-level `build.gradle.kts` (usually the `:app` or `:feature` module):

```kotlin
plugins {
    id("com.google.devtools.ksp") // Required
    id("io.github.donald-okara.precover")
}
```

## 2. Add KSP Dependency
Add the Precover KSP processor to your dependencies:

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

    rules {
        register("Theme Coverage") {
            enabled.set(true)
            weight.set(io.github.donald_okara.precover.rules.engine.RuleWeight.HIGH)
        }
    }
}
```

## 4. Available Tasks
- `precoverReport`: Generates coverage reports in `build/reports/precover/`.
- `precoverCheck`: Verifies coverage against the threshold. This is automatically linked to the standard `check` task.
