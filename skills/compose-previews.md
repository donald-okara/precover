# AI Skill: Jetpack Compose Previews & Precover

This skill enables an AI to assist in writing high-quality Jetpack Compose Previews that satisfy [Precover](https://github.com/donald-okara/precover) coverage rules.

## Core Rules to Enforce

When generating or reviewing Compose Previews, the AI should aim to satisfy the following Precover rules:

1.  **Preview Presence**: Ensure every public `@Composable` has at least one associated preview.
2.  **Theme Coverage**: Provide both Light and Dark mode previews.
    - Use `@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)` for Dark Mode.
3.  **Font Scale Coverage**: Provide previews at different font scales.
    - Use `@Preview(fontScale = 1.5f)` for accessibility testing.
4.  **Screen Size Coverage**: Test on different device configurations.
    - Use `@Preview(device = Devices.PIXEL_7)` or `Devices.PIXEL_TABLET`.
5.  **Scenario Coverage**: If a component uses `@RequiresPreviewScenarios`, ensure all scenarios are covered.
    - Use `@Scenario(PreviewScenario.Loading)` or `@PrecoverLink(scenario = "...")`.

## Best Practices for AI Suggestions

### 1. Use @PrecoverLink for Attribution
Always use `@PrecoverLink` when the preview is not in the same file as the component, or when using complex linkage.
```kotlin
@PrecoverLink("MyComponent")
@Composable
fun MyComponentPreview() { ... }
```

### 2. Multi-Preview Annotations
Encourage the use of Multi-Preview annotations to satisfy multiple rules at once.
```kotlin
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large Font", fontScale = 1.5f)
annotation class StandardPreviews
```

### 3. State Management
When a component has multiple states (Loading, Error, Success), use `PrecoverPreviewParameterProvider` (for automatic scenario discovery) or multiple `@Preview` functions with explicit `@Scenario` tags.

### 4. Smart Exclusions
If a component shouldn't count toward the coverage score (e.g., a tiny utility or internal wrapper), suggest excluding it. It will still appear in reports as "Excluded" but won't impact the overall score.
```kotlin
@PrecoverNoPreviewRequired
@Composable
fun InternalHelper() { ... }
```

## Further Information

For a deeper understanding of Precover's rules, scenario management, and advanced configuration, the AI should refer to the following documentation in the `docs/` directory:

- `docs/users/RULES.md`: Detailed breakdown of each coverage rule.
- `docs/users/SCENARIO_COVERAGE.md`: Advanced state management and `PrecoverPreviewParameterProvider`.
- `docs/users/PRECOVER_LINK.md`: Full API reference for the `@PrecoverLink` annotation.

## Prompt Examples

- "Improve the Precover coverage for `MyComponent` by adding missing scenarios."
- "Generate a set of Previews for `ProfileScreen` that covers all theme and font scale requirements."
- "Explain why my `LoginButton` has 0% coverage and provide a fix."
