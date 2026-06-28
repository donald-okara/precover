# Using @PrecoverLink

The `@PrecoverLink` annotation is the primary way to connect your `@Preview` functions to the `@Composable` components they test.

## Why use it?
Precover calculates coverage by analyzing which components have previews. While Precover can often guess the relationship via naming conventions or body analysis, `@PrecoverLink` provides an explicit, robust connection that supports advanced metadata.

## Usage Patterns

### 1. Linking by Name (Simple)
The default parameter accepts the string name of the target Composable.
```kotlin
@PrecoverLink("MyComponent")
@Composable
fun MyPreview() {
    MyComponent()
}
```

### 2. Type-Safe Linkage (Navigation Keys)
If your screen takes a unique data type (like a `NavKey`), you can link to it via the `target` class.
```kotlin
@PrecoverLink(target = ProfileDetails::class)
@Composable
fun ProfilePreview() {
    ProfileScreen(ProfileDetails(id = 1))
}
```

### 3. Marker Annotation Linkage
For components without unique parameters, use a custom marker annotation.
```kotlin
annotation class SettingsMarker

@SettingsMarker
@Composable
fun SettingsScreen() { ... }

@PrecoverLink(target = SettingsMarker::class)
@Composable
fun SettingsPreview() { ... }
```

## Configuration Parameters
`@PrecoverLink` supports all standard `@Preview` parameters, allowing you to configure the preview environment directly:
- `name`: Name shown in reports.
- `fontScale`: Test accessibility (e.g., `1.5f`).
- `uiMode`: Test Night Mode (e.g., `Configuration.UI_MODE_NIGHT_YES`).
- `device`, `locale`, `showBackground`, etc.
