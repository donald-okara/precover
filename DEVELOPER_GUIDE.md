# Precover Developer's Guide

Precover is a static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality across themes, font scales, and screen sizes.

---

## 🚀 For Users: Adding Precover to Your Project

### 1. Apply the Plugin
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
    id("io.github.donald_okara.precover")
}
```

### 2. Add KSP Dependency
Add the Precover KSP processor to your dependencies:

```kotlin
dependencies {
    "ksp"(project(":ksp")) // Replace with "io.github.donald-okara:precover-ksp:1.0.0" when published
}
```

### 3. Configure Precover
Customize the behavior using the `precover` extension:

```kotlin
precover {
    // Threshold for precoverCheck task (0-100)
    coverageThreshold.set(80f)
    // Enable/Disable report formats
    htmlReportEnabled.set(true)
    jsonReportEnabled.set(true)
}
```

### 4. Available Tasks
- `precoverReport`: Generates coverage reports in `build/reports/precover/`.
- `precoverCheck`: Verifies coverage against the threshold. This is automatically linked to the standard `check` task.

---

## 🛠 For Maintainers: Contributing to Precover

### Project Structure
- `:core`: Shared data models used by KSP, Rules, and the Plugin.
- `:ksp`: The Kotlin Symbol Processing engine that extracts `@Composable` and `@Preview` metadata into a JSON intermediate file.
- `:rules`: The evaluation engine. It takes the metadata and runs a set of pluggable rules to calculate coverage scores and identify violations.
- `:gradle-plugin`: Orchestrates the build lifecycle, linking KSP execution to report generation and threshold enforcement.

### Internal Workflow
1. **Extraction**: `PrecoverProcessor` (KSP) scans the source code for `@Composable` functions and their associated `@Preview` annotations (including Multi-previews).
2. **Intermediate Format**: Metadata is stored in `build/generated/ksp/.../precover-metadata.json`.
3. **Evaluation**: `RuleEngine` reads the metadata and applies `PrecoverRule` implementations (e.g., `ThemeRule`).
4. **Reporting**: Results are aggregated into a `CoverageReport` and rendered via `HtmlReporter` or `JsonReporter`.

### Adding a New Rule
To add a new coverage check:
1. Navigate to the `:rules` module.
2. Implement the `PrecoverRule` interface:
   ```kotlin
   class MyCustomRule : PrecoverRule {
       override val name: String = "My Custom Check"
       override fun evaluate(composable: ComposableMetadata): List<RuleViolation> {
           // Your logic here
       }
   }
   ```
3. Register the rule in `RuleEngine.kt`.

### Building and Testing
- Run `./gradlew build` to build all modules.
- Use the `:app` module for manual integration testing. It is configured with Navigation 3 and Adaptive layouts to ensure compatibility with modern Compose patterns.
