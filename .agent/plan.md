# Project Plan

Precover — Compose Preview Coverage System. A static analysis and reporting tool for Jetpack Compose that evaluates preview coverage quality. It is split into Core, KSP, Lint, Gradle Plugin, Rules, and Integration Tests modules.

## Project Brief

# Precover — Project Brief

Precover is a developer-focused static analysis tool and reporting system for Jetpack Compose. Similar to code coverage tools like JaCoCo or Kover, Precover treats Compose previews as structured test probes, measuring how well UI components are exercised across critical dimensions—such as themes, font scales, and screen sizes—to reduce regressions before manual QA.

## Features

- **Static Metadata Extraction**: Leverages KSP to analyze `@Composable` and `@Preview` annotations, building a deep model of UI code without requiring an active device or emulator.
- **Multi-Dimensional Coverage Analysis**: Evaluates components against a pluggable rule system to ensure they are previewed across Light/Dark modes, diverse Font Scales, and varied Screen Sizes.
- **Automated Reporting**: Generates actionable HTML, JSON, and text reports that highlight missing preview dimensions and provide an "Overall Coverage" score.
- **CI/CD Enforcement**: Provides a Gradle plugin with customizable thresholds to enforce UI quality standards during the build process and prevent coverage regressions.

## High-Level Technical Stack

- **Kotlin & Coroutines**: The foundation for high-performance static analysis and asynchronous report generation.
- **KSP (Kotlin Symbol Processing)**: The primary engine used to extract AST-like models from Compose codebases.
- **Gradle Plugin API**: For seamless integration into the Android build lifecycle and CI/CD pipelines.
- **Jetpack Compose**: The target framework; the system analyzes and validates Compose-based UI code.
- **Navigation & Adaptive Strategy**: The internal **Integration Test** suite strictly utilizes **Jetpack Navigation 3** and the **Compose Material Adaptive** library to validate that the tool correctly handles complex state-driven navigation and responsive layouts.

## Implementation Steps
**Total Duration:** 17m 43s

### Task_1_Core_and_KSP: Initialize the 'core' and 'ksp' modules. Define data structures for Composable and Preview metadata. Implement the KSP processor to scan code and extract this metadata into a structured model.
- **Status:** COMPLETED
- **Updates:** Initialized :core and :ksp modules.
- **Acceptance Criteria:**
  - Core data models (Component, Preview, Metadata) are defined
  - KSP processor successfully identifies @Composable and @Preview annotations
  - Metadata extraction works for basic Composable functions

### Task_2_Rule_Engine_and_Reporting: Implement the rule evaluation engine in a 'rules' module to check for theme, font scale, and screen size coverage. Create the reporting system to generate JSON and HTML summaries of the coverage analysis.
- **Status:** COMPLETED
- **Updates:** Created :rules module and implemented a pluggable PrecoverRule interface.
- **Acceptance Criteria:**
  - Rules for Multi-Dimensional coverage (Theme, Font, Screen) are implemented
  - Coverage score calculation logic is functional
  - JSON and HTML report generators produce valid files based on analysis
- **Duration:** 8m 32s

### Task_3_Gradle_Plugin: Develop the Gradle plugin module to orchestrate the KSP task and the reporting engine. Add configuration options for coverage thresholds and report output paths.
- **Status:** COMPLETED
- **Updates:** Created :gradle-plugin module.
Implemented PrecoverExtension for DSL configuration (coverageThreshold, htmlReportEnabled, jsonReportEnabled).
Developed PrecoverReportTask to orchestrate metadata reading, rule evaluation, and report generation.
Developed PrecoverCheckTask to enforce coverage thresholds and fail the build on violations.
Configured task dependencies to ensure KSP analysis runs before reporting.
Integrated precoverCheck with the standard 'check' task.
Verified plugin assembly and module linkage.
- **Acceptance Criteria:**
  - Gradle plugin is successfully applied to a project
  - Plugin triggers KSP analysis and reporting as part of the build lifecycle
  - Customizable thresholds for CI/CD enforcement are working
- **Duration:** 9m 11s

### Task_4_Verification: Run the Precover tool on the 'app' module, which utilizes Jetpack Navigation 3 and Compose Adaptive layouts. Verify that the coverage reports accurately reflect the previews and that the build process is stable.
- **Status:** COMPLETED
- **Updates:** Verified Precover on :app module.
Successfully ran precoverCheck and precoverReport tasks.
Coverage score (83.0%) verified against 75% threshold.
Validated Navigation 3 and Adaptive layout coverage in generated JSON reports.
Confirmed build stability and unit test pass.
- **Acceptance Criteria:**
  - Build passes successfully with the plugin applied
  - Reports correctly identify coverage for Navigation 3 and Adaptive components
  - App does not crash and all existing tests pass
  - Stability and alignment with requirements are confirmed
- **StartTime:** 2026-06-23 12:17:09 EAT
- **EndTime:** 2026-06-23 13:45:00 EAT

