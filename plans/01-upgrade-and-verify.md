# Plan: Verify Tests, Upgrade Tooling and Dependencies

## Goal

Bring the repository onto current supported versions for Gradle, plugins, and library dependencies while preserving behavior and keeping the existing Groovy DSL intact. Start by proving the current baseline, then upgrade in controlled steps, fixing compatibility issues as they appear.

## Why This Matters

- The project is being used more often by coding agents and needs predictable, repeatable validation behavior.
- Up-to-date dependencies reduce maintenance risk and improve compatibility with newer JDKs, Gradle, and IDE tooling.
- A clean upgrade path will make later Java migrations easier.

## Constraints

- Keep the current Groovy DSL for rule configuration.
- Preserve current module structure unless an upgrade forces a change.
- Do not mix architectural refactors into this effort except where required by broken upgrades.
- Minimize behavioral changes to existing rules.

## Success Criteria

- Current tests pass before upgrades, or any existing failures are diagnosed and fixed first.
- All modules build on the target upgraded stack.
- All tests pass after upgrades.
- CLI, Maven plugin, and core library behavior remain functionally equivalent.
- `AGENTS.md` and nearby docs are updated if command behavior changes.

## Scope

- Gradle wrapper
- Gradle plugins from `buildSrc`
- Groovy / Spock / test stack
- Runtime libraries in core, CLI, SPI, and Maven plugin
- Build and publishing conventions that require updates for newer Gradle

## Out of Scope

- Native image support
- Replacing the Groovy rule DSL
- Large rule API redesigns
- Full Java migration of rules or parser infrastructure

## Phase 1: Establish Baseline

1. Run repository baseline commands:
   - `./gradlew test`
   - `./gradlew build`
2. Record current failures by module.
3. Determine whether failures are environmental, flaky, or due to stale code.
4. Fix any existing broken tests before upgrading anything.
5. Capture build timings and obvious slow steps to compare after upgrades.

## Phase 2: Inventory Current Versions

1. Catalog versions in:
   - `gradle/wrapper/gradle-wrapper.properties`
   - `buildSrc/build.gradle`
   - `buildSrc/src/main/groovy/*.gradle`
   - module `build.gradle` files
2. Identify compatibility matrix for:
   - Gradle
   - Groovy
   - Spock
   - Shadow plugin
   - Maven plugin development plugin
   - semver plugin
   - Gson / JSON / SnakeYAML / Reflections / Maven Invoker / Picocli
3. Confirm the highest practical Java baseline for the build tooling while preserving project runtime intent.

## Phase 3: Upgrade Strategy

Upgrade in small layers so breakage is attributable.

### Layer A: Gradle Wrapper and Build Plugins

1. Upgrade Gradle wrapper to a supported current version.
2. Update `buildSrc` plugin dependencies to versions compatible with that Gradle release.
3. Fix deprecations or removed APIs in convention plugins.
4. Verify:
   - `./gradlew help`
   - `./gradlew test`

### Layer B: Test and Language Stack

1. Re-evaluate Groovy 3.x and Spock versions.
2. Upgrade Spock within the Groovy-major version that preserves the current DSL and parser behavior.
3. Avoid a Groovy major upgrade unless necessary for tooling compatibility.
4. Verify all tests after each change.

### Layer C: Runtime Libraries

1. Upgrade core runtime libraries one at a time or in small compatible groups.
2. Pay special attention to:
   - `org.reflections`
   - `snakeyaml`
   - `gson`
   - `org.json`
   - `maven-invoker`
   - `picocli`
3. Re-run targeted module tests after each library family update.

### Layer D: Publish / Distribution Tasks

1. Verify publishing tasks still work:
   - `publish`
   - `publishAllPublicationsToStagingRepository`
2. Verify CLI distribution tasks still work:
   - `:mule-linter-cli:installDist`
   - `:mule-linter-cli:shadowDistTar`
   - `:mule-linter-cli:shadowDistZip`
   - `:mule-linter-cli:renameDists`

## Phase 4: Fixes Likely Needed

Expect to touch these areas during upgrades:

- Convention plugin syntax for newer Gradle APIs
- Shadow plugin task wiring
- Groovy test annotations or JUnit Platform integration
- `Reflections` usage if package scanning behavior changed
- Maven plugin development plugin behavior under newer Gradle
- Publishing conventions if metadata DSL changed

## Verification Matrix

Run at least:

- `./gradlew test`
- `./gradlew build`
- `./gradlew :mule-linter-core:test`
- `./gradlew :mule-linter-spi:test`
- `./gradlew :mule-linter-spi-test:test`
- `./gradlew :mule-linter-maven-plugin:test`
- `./gradlew :mule-linter-cli:installDist`

If publishing logic changed, also run:

- `./gradlew publishAllPublicationsToStagingRepository`

## Risks

- Newer Gradle may expose deprecated build logic in `buildSrc`.
- Groovy and Spock compatibility may constrain upgrade order.
- `Reflections` may behave differently, affecting DSL rule loading.
- Maven Invoker behavior may shift under newer dependency versions.
- Shadow packaging task names or outputs may change.

## Recommended Execution Order

1. Baseline tests and fix existing failures.
2. Upgrade Gradle wrapper.
3. Upgrade `buildSrc` plugins and fix build logic.
4. Upgrade Groovy/Spock only if necessary.
5. Upgrade runtime libraries in small groups.
6. Run full build and distribution tasks.
7. Update docs if any commands or expectations changed.

## Deliverables

- Updated wrapper and dependency versions
- Passing test suite
- Notes on compatibility decisions and pinned versions
- Any required doc updates in `README.md` or `AGENTS.md`
