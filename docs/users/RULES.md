# Built-in Coverage Rules

Precover comes with a set of built-in rules that evaluate the quality of your Compose Previews. Each rule has a **Weight** (impact on score) and a **Severity** (how critical a violation is).

---

## 1. Preview Presence
- **Rule Name**: `Preview Presence`
- **Weight**: `MANDATORY`
- **Severity**: `ERROR`

This is the most fundamental rule. It checks if a `@Composable` has *any* associated previews (via `@Preview`, `@PrecoverLink`, naming conventions, or smart inference).

**Impact**: Since it is `MANDATORY`, if a component has no previews, its coverage score will be **0%** regardless of other rules.

---

## 2. Theme Coverage
- **Rule Name**: `Theme Coverage`
- **Weight**: `HIGH`
- **Severity**: `WARNING`

Checks if the component is previewed in both Light and Dark modes.
- **Light Mode**: Detected if `uiMode` is unset, `0`, or explicitly `UI_MODE_NIGHT_NO`.
- **Dark Mode**: Detected if `uiMode` is `UI_MODE_NIGHT_YES`.

**Recommendation**: Add a `@Preview` with `uiMode = Configuration.UI_MODE_NIGHT_YES` to satisfy this rule.

---

## 3. Font Scale Coverage
- **Rule Name**: `Font Scale Coverage`
- **Weight**: `MEDIUM`
- **Severity**: `INFO`

Checks if the component is tested with accessibility settings in mind. It expects at least **two different font scales** to be used across all previews for a component.

**Recommendation**: Add a preview with `fontScale = 1.5f` (or similar) to ensure your layout doesn't break when users increase their system font size.

---

## 4. Screen Size Coverage
- **Rule Name**: `Screen Size Coverage`
- **Weight**: `MEDIUM`
- **Severity**: `INFO`

Checks if the component is previewed on different screen configurations. It looks for previews that explicitly define a `device` string, or set `widthDp` / `heightDp`.

**Recommendation**: For complex screens, add a preview using a tablet device (e.g., `device = Devices.PIXEL_TABLET`) to ensure adaptive layouts work as expected.

---

## Exclusion Rules

Components can be excluded from these rules using special annotations. See [Exclusions](EXCLUSIONS.md) for more details.
- **@PrecoverNoPreviewRequired**: Completely excludes a component from coverage scoring.
- **@PrecoverIgnoreScenarios**: Excludes a component from scenario validation (optionally targeting specific scenarios).
