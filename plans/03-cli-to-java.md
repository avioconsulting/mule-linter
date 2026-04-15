# Plan: Move CLI to Java

## Goal

Migrate the CLI entrypoint from Groovy to Java while preserving command-line behavior, distribution packaging, and integration with the existing core engine and Groovy rule DSL.

## Why This Matters

- The CLI is small and isolated, making it a low-risk Java migration target.
- A Java CLI is easier for agents and IDE tooling to maintain.
- It reduces Groovy runtime surface in the user-facing executable path.
- It aligns the CLI with the Maven plugin, which is already Java.

## Constraints

- Keep command-line flags and behavior stable.
- Keep `picocli` as the command framework unless an upgrade requires otherwise.
- Keep current distribution tasks working.
- Do not change core rule execution behavior as part of this plan.

## Success Criteria

- CLI main class is implemented in Java.
- Existing CLI options still work:
  - `--rules`
  - `--dir`
  - `--format`
- CLI distributions are still produced by Gradle.
- CLI integration tests or smoke tests pass.

## Current State

File:

- `mule-linter-cli/src/main/groovy/com/avioconsulting/mule/linter/MuleLinterCli.groovy`

Current responsibilities:

- parse command-line options with `picocli`
- construct `MuleLinter`
- call `runLinter()`

This migration is mechanically simple.

## Proposed Implementation

### Phase 1: Java Entry Point

Create a Java equivalent of `MuleLinterCli` in:

- `mule-linter-cli/src/main/java/com/avioconsulting/mule/linter/MuleLinterCli.java`

Preserve:

- command name
- option names
- required/optional flags
- default format behavior
- help text as closely as practical

### Phase 2: Build Script Updates

1. Confirm `application.mainClass` still points to the same fully qualified class name.
2. Ensure mixed Java/Groovy compilation still works while core remains Groovy-based.
3. Verify Shadow and distribution tasks still package the CLI correctly.

### Phase 3: Verification

Smoke test:

- help output
- invalid args handling
- successful execution against sample project/config

Recommended commands:

- `./gradlew :mule-linter-cli:build`
- `./gradlew :mule-linter-cli:installDist`
- `./gradlew :mule-linter-cli:shadowDistTar :mule-linter-cli:shadowDistZip`

If practical, add a focused CLI test that validates argument wiring without depending on a full end-to-end build.

## Risks

- Package/class name changes could break the `application` plugin entrypoint.
- Picocli annotations need to be translated carefully to preserve help text and defaults.
- Mixed Java/Groovy source layout may require minor build adjustments.

## Nice-to-Haves

- Improve CLI error messaging around missing Maven home for effective-pom generation.
- Add a focused smoke test using a sample Mule app.
- Consider a tiny adapter layer so the CLI stays thin even if core constructor changes later.

## Deliverables

- Java CLI main class
- preserved packaging/distribution behavior
- smoke-test coverage or equivalent verification notes
