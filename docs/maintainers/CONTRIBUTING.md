# Contributing to Precover

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
