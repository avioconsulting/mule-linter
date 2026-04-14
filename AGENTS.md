# AGENTS Guide

This file is for coding agents working in `mule-linter`.

## Repository Shape

- Build system: Gradle.
- Language mix: mostly Groovy, plus a small Java-based Maven plugin module.
- Test framework: Spock on JUnit Platform.
- Java toolchain: Java 8.
- Modules declared in `settings.gradle`:
- `mule-linter-spi`
- `mule-linter-core`
- `mule-linter-spi-test`
- `mule-linter-maven-plugin`
- `mule-linter-cli`

## Build Commands

- Full build: `./gradlew build`
- Compile without tests: `./gradlew classes testClasses`
- Run all tests: `./gradlew test`
- Build one module: `./gradlew :mule-linter-core:build`
- Build CLI distribution: `./gradlew :mule-linter-cli:installDist`
- Build publishable artifacts locally: `./gradlew publish`
- Stage Maven-publish output locally: `./gradlew publishAllPublicationsToStagingRepository`

## Test Commands

- Run all tests in the repo: `./gradlew test`
- Run tests for one module: `./gradlew :mule-linter-core:test`
- Run one test class: `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.configuration.LoggerAttributesRuleTest'`
- Run one Spock feature method: `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.configuration.LoggerAttributesRuleTest.Logger Attributes check'`
- Run SPI module tests: `./gradlew :mule-linter-spi:test`
- Run SPI extension tests: `./gradlew :mule-linter-spi-test:test`
- Run Maven plugin tests: `./gradlew :mule-linter-maven-plugin:test`

## Lint / Static Analysis

- Primary quality gate in practice: `./gradlew build`
- There is commented-out CodeNarc configuration in `buildSrc/src/main/groovy/mule-linter.groovy-common-conventions.gradle`.
- The root README mentions `gradle check` and CodeNarc reports, but CodeNarc is not currently enabled in Gradle conventions.
- Use `./gradlew check` if you want the standard Gradle verification lifecycle.
- Do not assume Checkstyle, Spotless, or ErrorProne are configured.

## Other Useful Commands

- Generate the IntelliJ GDSL file: `./gradlew :mule-linter-core:generateGDSL`
- Build CLI archives: `./gradlew :mule-linter-cli:shadowDistTar :mule-linter-cli:shadowDistZip`
- Rename CLI shadow distributions to final names: `./gradlew :mule-linter-cli:renameDists`
- List available tasks for one module: `./gradlew :mule-linter-core:tasks --all`

## Single-Test Guidance

- Prefer `--tests` over editing build files.
- Use the fully qualified class name for stable execution.
- For Spock feature methods, Gradle accepts `ClassName.feature name`.
- Quote `--tests` arguments because feature names contain spaces.
- If unsure about a class name, search `src/test/groovy` first.

Examples:

- `./gradlew :mule-linter-spi:test --tests 'com.avioconsulting.mule.linter.model.CaseNamingTest'`
- `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.pom.MunitVersionRuleTest'`
- `./gradlew :mule-linter-spi-test:test --tests 'com.aviconsulting.mule.linter.extension.HttpTests'`

## CI Signals

- CI uses `.github/workflows/build.yml`.
- Main workflow delegates to shared Gradle workflows.
- Branch patterns explicitly included for push builds: `main`, `chore/**`, `feat/**`.
- The workflow points at `mule-linter-core` for versioning.

## Editor / Agent Rules

- No `.cursorrules` file was found.
- No files were found under `.cursor/rules/`.
- No `.github/copilot-instructions.md` file was found.
- Treat this `AGENTS.md` as the repository-specific agent instruction source.

## Source Layout

- Main Groovy code lives under `*/src/main/groovy`.
- Tests live under `*/src/test/groovy`.
- The Maven plugin module keeps Java sources in `mule-linter-maven-plugin/src/main/java`.
- Test fixtures include Mule sample apps under `src/test/resources/SampleMuleApp`.
- Generated or support docs/config live under `config/`, `docs/`, and module READMEs.

## Code Style: General

- Follow existing local style before introducing new patterns.
- Prefer small, local changes over broad refactors.
- Preserve public APIs unless the task explicitly calls for a breaking change.
- Keep module boundaries intact: SPI models in `mule-linter-spi`, execution/rules in `mule-linter-core`, plugin glue in `mule-linter-maven-plugin`, CLI wiring in `mule-linter-cli`.
- Match surrounding language style exactly: Groovy style in Groovy files, Java style in Java files.

## Code Style: Formatting

- Indentation is 4 spaces.
- Braces are on the same line for classes, methods, constructors, and control flow.
- Existing Groovy code often omits semicolons; keep omitting them unless the file already uses Java syntax.
- Existing Java code uses standard semicolons and explicit types.
- Keep line wrapping conservative and consistent with nearby code rather than reformatting whole files.
- Preserve existing blank-line rhythm between imports, fields, methods, and logical sections.

## Code Style: Imports

- Prefer explicit imports.
- Follow the current file’s ordering instead of applying an external formatter’s idea of order.
- Avoid wildcard imports unless the file already uses them and there is a strong local reason.
- In Java files, static imports are used sparingly; only add them when they improve readability.

## Code Style: Types

- Groovy code in this repo is mixed-typed, not strictly typed.
- Use explicit types when they improve clarity for fields, method signatures, and important locals.
- `def` is common for short-lived locals and Spock lifecycle methods.
- Reuse existing domain types such as `Rule`, `RuleSet`, `RuleViolation`, `MuleApplication`, and `ComponentIdentifier` instead of inventing parallel abstractions.
- Do not introduce `@CompileStatic` or broad typing policy changes unless required.

## Code Style: Naming

- Class names use PascalCase.
- Methods and fields use camelCase.
- Constants use uppercase snake case only when the surrounding code does so.
- Rule classes are consistently named `*Rule`.
- Test classes are consistently named `*Test` or, in one SPI extension case, `HttpTests`; follow local precedent for the module you are touching.
- Keep package names aligned with the existing module namespace.
- Be careful: there is at least one existing package typo/inconsistency in tests (`com.aviconsulting...` vs `com.avioconsulting...`). Do not “fix” package names unless that is part of the task.

## Code Style: Groovy and Spock

- Spock specs extend `spock.lang.Specification`.
- Common structure is `given:`, `when:`, `then:` with readable feature names in single quotes.
- Test helper state is often initialized in `setup()` and cleaned in `cleanup()`.
- Keep tests concrete and example-driven.
- Prefer adding or updating focused specs near the affected rule/module.

## Code Style: Error Handling

- Follow the local error handling style of the module.
- In Groovy core code, failures are often allowed to propagate naturally.
- In the Maven plugin module, errors are logged and build failure is decided explicitly via `failIfNeeded`.
- Do not swallow exceptions silently.
- If catching exceptions, either log meaningful context or convert them to the module’s expected failure mode.
- Preserve user-facing behavior around `failBuild` and severity handling.

## Code Style: Comments and Documentation

- Keep comments sparse and practical.
- Add comments only where behavior is non-obvious.
- Preserve existing README and Groovy DSL examples when changing user-visible behavior.
- If you change a command, task name, or plugin parameter, update the nearest README if needed.

## Module-Specific Guidance

- `mule-linter-spi`: shared model and SPI contracts. Changes here ripple outward; avoid casual API churn.
- `mule-linter-core`: parsing, DSL loading, rule execution, and built-in rules. Most behavior changes belong here.
- `mule-linter-spi-test`: extension example and integration-style validation of the SPI.
- `mule-linter-maven-plugin`: Maven-facing wrapper; keep it thin and compatible with Maven conventions.
- `mule-linter-cli`: command-line packaging and distribution.

## Parent POM Resolution

Parent POMs are resolved using embedded Maven Resolver (Eclipse Aether):
- Resolves full parent chain (child → parent → grandparent → ...)
- Uses ~/.m2/settings.xml for repository configuration and authentication
- Caches resolved parents in ~/.m2/repository (standard Maven cache)
- Only resolves parent POMs (not dependencies or plugins)

**New Resolution Methods:**
- `PomFile.resolveProperty(String)` - Returns ResolvedProperty with source tracking
- `PomFile.resolveDependency(String, String)` - Returns ResolvedDependency with inheritance info
- `PomFile.resolvePlugin(String, String)` - Returns ResolvedPlugin with management info

**Backward Compatibility:**
- Existing methods (`getPomProperty()`, `getDependency()`, `getPlugin()`) still work
- Parent resolution is explicit - call `pomFile.resolveParents(ParentPomResolver.getInstance())` to enable inheritance
- Parent resolution failures are logged as warnings (not fatal)

**System Properties:**
- `mule.linter.localRepo`: Custom local repository path (default: ~/.m2/repository)

**Environment:**
- `M2_HOME`: Used to find global settings.xml

## Consolidated POM Test Structure

The `mule-linter-core` module has consolidated POM-related tests into 4 comprehensive test classes:

- `PomVersionRuleTest` (24 tests): Version-related tests for Mule Maven Plugin, MUnit, Mule Runtime, APIKit, and generic dependency versions
- `PomPropertyRuleTest` (16 tests): Property validation, plugin attributes, MUnit Maven Plugin attributes, and POM existence checks
- `PomManagementRuleTest` (9 tests): Tests for dependencyManagement, pluginManagement, and parent POM structure validation
- `EffectivePomIntegrationTest` (11 tests): Integration tests for effective POM generation and PomFile API access

Key features:
- All POM tests use the new `ComprehensiveParentSample` structure for parent-child POM testing
- `TestApplication.addComprehensiveParentSample()` creates parent/child POM fixtures
- `TestApplication.useEffectivePomGeneration()` enables effective POM generation (requires Maven network access for parent resolution)
- Tests that don't require network access use embedded POM strings for fast, isolated execution

Old test files replaced by this consolidation (15 files):
- `MuleMavenPluginVersionRuleTest`, `MunitVersionRuleTest`, `MunitPluginVersionRuleTest`, `MuleRuntimeVersionRuleTest`, `ApikitVersionRuleTest`
- `PomDependencyVersionRuleTest`, `PomPropertyValueRuleTest`, `PomArtifactAttributeRuleTest`, `PomExistsRuleTest`
- `MunitMavenPluginAttributesRuleTest`

## Testing Expectations For Changes

- Run the narrowest relevant module test task during iteration.
- Before finishing, prefer the smallest command that proves the change, then escalate to a broader build if the change crosses modules.
- For changes in shared SPI or build logic, run at least all affected module tests.
- For changes in Gradle conventions or publication logic, note clearly if you did not run a full build.
- For POM-related changes, run: `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.pom.*'`

## Safe Working Practices

- Do not edit generated artifacts unless the task specifically requires regeneration.
- If you modify `generateGDSL` inputs or DSL behavior, consider running `:mule-linter-core:generateGDSL`.
- Be cautious around release/version files like `version.properties` and `jreleaser.yml`.
- Sample Mule apps under test resources are fixtures; update them only when the tests require it.

## Good Defaults For Agents

- Search before editing.
- Read the relevant module `build.gradle` and nearby tests before changing behavior.
- Prefer module-scoped Gradle commands to keep feedback fast.
- Verify with tests whenever behavior changes.
- If a command from README conflicts with actual Gradle config, trust the checked-in build files and mention the discrepancy.
