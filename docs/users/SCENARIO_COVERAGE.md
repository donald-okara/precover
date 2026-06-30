# Preview Scenario Coverage

Precover allows you to enforce coverage for specific UI scenarios (e.g., Loading, Error, Success) to ensure your components are fully tested across all states.

## Declaring Required Scenarios

Use the `@RequiresPreviewScenarios` annotation on your `@Composable` components to specify which states must be previewed.

```kotlin
@RequiresPreviewScenarios(PreviewScenario.Loading, PreviewScenario.Success, "EmptyState")
@Composable
fun UserProfile(state: UserUiState) {
    // ...
}
```

## Mapping Previews to Scenarios

You can satisfy these requirements by marking your preview functions with the `@Scenario` annotation.

```kotlin
@Preview
@Scenario(PreviewScenario.Loading)
@Composable
fun UserProfileLoadingPreview() {
    UserProfile(state = UserUiState.Loading)
}
```

## Using @PrecoverLink

If your previews are located in a separate file or use a helper function, you can use `@PrecoverLink` with the `scenario` parameter.

```kotlin
@Preview
@PrecoverLink("UserProfile", scenario = PreviewScenario.Success)
@Composable
fun UserProfileSuccessPreview() {
    UserProfile(state = UserUiState.Success(...))
}
```

## Provider-based Approach (Recommended)

If you use `PreviewParameterProvider` to provide multiple states to your previews, you can extend `PrecoverPreviewParameterProvider` and use the `scenario()` helper to automatically derive coverage.

```kotlin
class UserProvider : PrecoverPreviewParameterProvider<UserUiState>() {
    override val values = sequenceOf(
        scenario(PreviewScenario.Loading, UserUiState.Loading),
        scenario(PreviewScenario.Error, UserUiState.Error),
        scenario(PreviewScenario.Success, UserUiState.Success(...))
    )
}

@Preview
@Composable
fun UserProfilePreview(
    @PreviewParameter(UserProvider::class) state: UserUiState
) {
    UserProfile(state = state)
}
```

By using this approach, the `UserProfilePreview` will automatically satisfy the required scenarios for `UserProfile` without additional annotations on the preview function itself.

## Built-in Constants

For convenience, Precover provides a `PreviewScenario` object with common scenario names:
- `PreviewScenario.Loading` ("Loading")
- `PreviewScenario.Error` ("Error")
- `PreviewScenario.Success` ("Success")
- `PreviewScenario.Empty` ("Empty")
- `PreviewScenario.Disabled` ("Disabled")
- `PreviewScenario.ReadOnly` ("ReadOnly")

You can also use any custom string as a scenario name.
