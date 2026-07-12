# Preview Exclusions and Ignore Rules
[![Maven Central](https://img.shields.io/maven-central/v/io.github.donald-okara/core)](https://central.sonatype.com/artifact/io.github.donald-okara/core)

Not all Composables require full preview coverage. Precover provides a mechanism to exclude certain components or specific scenarios from enforcement, keeping your reports meaningful and reducing noise.

## 1. Full Exclusion (@PrecoverNoPreviewRequired)

For components that are too simple to require previews (e.g., internal dividers, low-level UI primitives) or debug-only helpers, use `@PrecoverNoPreviewRequired`.

```kotlin
@PrecoverNoPreviewRequired
@Composable
fun InternalDivider() {
    Divider()
}
```

**Behavior**:
- The component still appears in reports but is marked as **Excluded**.
- It does **not** contribute to the module's overall coverage score.
- The `Preview Presence` rule is skipped for this component.

---

## 2. Ignoring Scenarios (@PrecoverIgnoreScenarios)

Sometimes a component needs previews for its main state but not for every required scenario declared at the module level, or you want to opt-out of specific state requirements.

### Opt-out of All Scenario Validation
If you want to ignore all scenario-based checks (but still enforce things like Theme or Font Scale), use the annotation without parameters:

```kotlin
@PrecoverIgnoreScenarios
@Composable
fun SimpleIcon() {
    Icon(Icons.Default.Add, contentDescription = null)
}
```

### Opt-out of Specific Scenarios
If only certain scenarios are irrelevant for a component, you can exclude them explicitly:

```kotlin
@PrecoverIgnoreScenarios(exclude = [PreviewScenario.Loading, PreviewScenario.Error])
@RequiresPreviewScenarios(PreviewScenario.Loading, PreviewScenario.Success)
@Composable
fun Header(state: HeaderState) {
    // ...
}
```
In this example, the `Loading` requirement will be ignored, but `Success` will still be enforced if it's not provided.

---

## 3. Guardrails Against Abuse

To prevent developers from overusing exclusions to bypass coverage metrics, Precover includes a guardrail that can be configured in your `build.gradle.kts`.

```kotlin
precover {
    // Fail the build if more than 20% of components are excluded
    maxExcludedRatio.set(0.2f)
}
```

If the ratio of excluded components exceeds this value, the `precoverCheck` task will fail, ensuring transparency and accountability in the coverage process.
