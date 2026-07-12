# Contributing to Precover
[![Maven Central](https://img.shields.io/maven-central/v/io.github.donald-okara.precover/core)](https://central.sonatype.com/artifact/io.github.donald-okara.precover/core)

## Project Structure
- `:core`: Shared data models used by KSP, Rules, and the Plugin.
- `:ksp`: The Kotlin Symbol Processing engine that extracts `@Composable` and `@Preview` metadata into a JSON intermediate file.
- `:rules`: The evaluation engine. It takes the metadata and runs a set of pluggable rules to calculate coverage scores and identify violations.
- `:gradle-plugin`: Orchestrates the build lifecycle, linking KSP execution to report generation and threshold enforcement.

## Internal Workflow
1. **Extraction**: `PrecoverProcessor` (KSP) scans the source code for `@Composable` functions and their associated `@Preview` or `@PrecoverLink` annotations.
2. **Intermediate Format**: Metadata is stored in `build/generated/ksp/.../precover-metadata.json`.
3. **Evaluation**: `RuleEngine` reads the metadata and applies `PrecoverRule` implementations (e.g., `ThemeRule`).
4. **Reporting**: Results are aggregated into a `CoverageReport` and rendered via `HtmlReporter` or `JsonReporter`.

## Adding a New Rule
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

## Smart Linkage Logic
The `PrecoverProcessor` uses several strategies to link previews:
1. **Explicit**: `@PrecoverLink` targets.
2. **Contextual**: Matches via `NavKey` types or Marker annotations.
3. **Inferred**: Scanning the function body for calls to known Composables.
4. **Naming**: Fallback to `ComposableNamePreview` convention.

## Testing Your Changes Locally

Since Precover is a Gradle plugin that depends on itself for reporting, testing changes requires a two-step "bootstrap" process.

### 1. Publish to Maven Local
Before running reports, you must publish the latest version of the plugin and its dependencies to your local Maven repository. Use the `precover.enabled=false` flag to prevent the plugin from trying to run on itself during its own build:

```bash
./gradlew :core:publishToMavenLocal :rules:publishToMavenLocal :ksp:publishToMavenLocal :gradle-plugin:publishToMavenLocal -Pprecover.enabled=false
```

**Testing in an external project:**
If you want to test your local changes in another project, publish with a specific version:
```bash
./gradlew publishToMavenLocal -Pprecover.version=1.0.3-SNAPSHOT -Pprecover.enabled=false
```
Then, ensure your external project includes `mavenLocal()` in its repository sources and update the version to match:

```kotlin
// In settings.gradle.kts or build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}
```

### 2. Run Quality Checks & Reports
Once published, you can run the full suite of checks and generate coverage reports for the `:app` module or the entire project:

**Run all CI checks (Spotless, Build, Tests):**
```bash
./gradlew spotlessCheck build
```

**Generate Aggregate Reports:**
```bash
./gradlew precoverAggregateReport precoverAggregateCheck
```

**Generate module-specific Report:**
```bash
./gradlew :app:precoverReport
```

### Simulating CI Workflows
To ensure your PR passes CI, you can run the equivalent of our GitHub Actions locally:

**Run Lint Checks (`lint.yml`):**
```bash
./gradlew :core:publishToMavenLocal :rules:publishToMavenLocal :ksp:publishToMavenLocal :gradle-plugin:publishToMavenLocal -Pprecover.enabled=false
./gradlew spotlessCheck build :app:precoverReport
```

**Run Precover Checks (`precover_check.yml`):**
```bash
./gradlew :core:publishToMavenLocal :rules:publishToMavenLocal :ksp:publishToMavenLocal :gradle-plugin:publishToMavenLocal -Pprecover.enabled=false
./gradlew precoverAggregateReport precoverAggregateCheck
```

The generated reports can be found in:
- Project-wide: `build/reports/precover/aggregate/`
- App Module: `app/build/reports/precover/`
