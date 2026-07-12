# Getting Started with Precover
[![Maven Central](https://img.shields.io/maven-central/v/io.github.donald-okara.precover/core)](https://central.sonatype.com/artifact/io.github.donald-okara.precover/core)

Precover is a static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality across themes, font scales, and screen sizes.

## 1. Setup Version Catalog

The recommended way to use Precover is via a version catalog. Add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
precover = "1.0.2" # Use the latest version

[libraries]
precover-core = { group = "io.github.donald-okara.precover", name = "core", version.ref = "precover" } # Optional
precover-ksp = { group = "io.github.donald-okara.precover", name = "ksp", version.ref = "precover" } # Optional

[plugins]
precover-root = { id = "io.github.donald-okara.precover", version.ref = "precover" }
precover = { id = "io.github.donald-okara.precover.module", version.ref = "precover" } # Optional
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

## Baseline Workflow

The Baseline Workflow allows you to use your current coverage as a "floor" for future checks. This is useful when you want to prevent coverage from dropping, but aren't ready to meet a hard global threshold yet.

### 1. Record a Baseline
Run the following command to record your current coverage as the baseline:

```bash
./gradlew precoverUpdateBaseline
```

This will create or update `precover/baselines.json` at your project root. This file should be committed to version control.

### 2. Coverage Checks
Once a baseline is recorded, `precoverCheck` will pass if:
- Current score >= `coverageThreshold`
- **OR** Current score >= Recorded Baseline

### 3. Improving Baselines
The `precoverUpdateBaseline` task only updates the baseline if the current score is **higher** than the existing one. This helps you track and maintain improvements over time.

### 4. Configuration
You can disable the baseline workflow or change the file location:

```kotlin
precover {
    // Disable baseline comparison (default is true)
    useBaseline.set(false)
    
    // Custom baseline file location
    baselineFile.set(layout.projectDirectory.file("custom-baselines.json"))
}
```

## Continuous Integration (CI/CD)

You can easily integrate Precover into your GitHub Actions workflows to automate coverage checks and baseline updates.

> [!IMPORTANT]
> The workflow examples below contain a **"Publish to Maven Local (Dev Only)"** step. This is specific to the Precover repository's development environment. **When copying these workflows to your own project, you should remove this step and the `PRECOVER_LOCAL_DEV` environment variable.**

### 1. Precover Check Workflow
This workflow runs on every pull request to ensure that the new changes meet the required coverage threshold or baseline.

```yaml
name: Precover Check

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  precover-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Run Precover Check
        run: ./gradlew precoverAggregateReport precoverAggregateCheck
```

### 2. Update Baseline Workflow
This workflow automatically updates your coverage baseline when changes are merged into the `main` branch.

```yaml
name: Update Precover Baseline

on:
  push:
    branches: [ main ]

jobs:
  update-baseline:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
        with:
          ref: main
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Run Precover Update Baseline
        run: ./gradlew precoverUpdateBaseline

      - name: Commit and Push Baseline
        run: |
          git config --global user.name "github-actions[bot]"
          git config --global user.email "github-actions[bot]@users.noreply.github.com"
          if git status --porcelain | grep -q "precover/baselines.json"; then
            git add precover/baselines.json
            git commit -m "chore: update precover baseline [skip ci]"
            git push origin main
          fi
```
