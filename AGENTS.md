# Agent Guide

## Build and focused verification

- Use `./gradlew` (wrapper: Gradle 9.4.1); module toolchains target Java 17. Shared build/test/publication behavior lives in `buildSrc/src/main/groovy/*conventions.gradle`, not a root build file.
- Full verification: `./gradlew build`. Compile main and test sources without running tests: `./gradlew classes testClasses`.
- Core tests: `./gradlew :mule-linter-core:test`.
- One Spock class: `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.configuration.LoggerAttributesRuleTest'`.
- One feature: append `.Logger Attributes check` inside that quoted test filter.
- POM-rule changes: `./gradlew :mule-linter-core:test --tests 'com.avioconsulting.mule.linter.rule.pom.*'`; inheritance changes also need SPI resolver tests and core model tests.
- SPI changes affect core, the Maven plugin, and the extension example; run affected module tests, not just `:mule-linter-spi:test`.
- CodeNarc is commented out in the shared conventions despite the README's claim. `check` does not produce the advertised CodeNarc report.

## Boundaries and discovery

- `mule-linter-spi` owns shared models (including the base `Rule`, `PomFile`, and XML parser), component SPI, and embedded Maven Resolver. `mule-linter-core` owns application loading, DSL, execution/reporting, and built-in rules.
- Execution entrypoint: core's `com.avioconsulting.mule.MuleLinter` constructs `MuleApplication`, evaluates the Groovy DSL, then runs `RuleExecutor`. CLI (`MuleLinterCli`) and Java Maven mojos wrap core.
- Rules are discovered by `RulesLoader` through Reflections under `com.`, `org.`, and `io.` using each class's `RULE_ID`; the DSL instantiates them and calls `init()`. New rule IDs must be unique.
- Components use a different mechanism: Java `ServiceLoader` with `META-INF/services/com.avioconsulting.mule.linter.spi.ComponentsFactory`. See `mule-linter-spi-test` for an extension example.
- Extension tests use the misspelled package `com.aviconsulting.mule.linter.extension`; use it in `--tests` filters rather than the usual `com.avioconsulting`.

## POM resolution and test traps

- Current application loading parses `pom.xml` and resolves only the parent chain through embedded Maven Resolver; it does **not** invoke Maven to generate `effective-pom.xml`. README Maven-invoker setup instructions are stale.
- Direct `PomFile` callers must call `resolveParents(...)` to enable inheritance. Use `resolveProperty`, `resolveDependency`, and `resolvePlugin` for source/inheritance-aware results. `MuleApplication` resolves parents during construction but logs a warning and continues on failure.
- Resolver settings/authentication come from Maven settings. Local repository priority: constructor path, `mule.linter.localRepo`, settings' `localRepository`, then `~/.m2/repository`.
- Never close `ParentPomResolver.getInstance()` in application code: its shutdown hook owns cleanup. Isolated tests can construct `new ParentPomResolver(path)` and close that instance themselves.
- Gradle tests set `mule.linter.skipEffectivePom=true`, but current application/resolver code does not read that flag; parent resolution can still access repositories. Use local parent fixtures or minimal POMs for isolated tests.
- `EffectivePomIntegrationTest` still skips unless `maven.home` or `MAVEN_HOME` is set, even though application loading no longer invokes Maven. A green default test run may omit this suite.
- Core's `TestApplication` creates temporary apps from `SampleMuleApp`; `addMinimalPom` and `addComprehensiveParentSample` support focused POM cases. Remove the temporary app in cleanup; if using `useEffectivePomGeneration()`, also call the helper's `cleanup()` to restore its system property.

## Generated artifacts and publication

- IntelliJ GDSL is generated from `mule-linter-core.gdsl.vm` and rule classes into `mule-linter-core/build/classes/groovy/main/mule-linter-core.gdsl`; do not edit the output. Regenerate with `./gradlew :mule-linter-core:generateGDSL` (also wired into test compilation, jars, and Groovydoc).
- CLI install: `./gradlew :mule-linter-cli:installDist`. Shadow archives: `./gradlew :mule-linter-cli:renameDists` (builds archives and removes `-shadow` from distribution names).
- `./gradlew publish` stages Maven artifacts in each publishing module's `build/staging-deploy`; it does not install them into `~/.m2`. Use `./gradlew publishToMavenLocal` for local Maven consumers.
- `.github/workflows/build.yml` delegates build/release to shared workflows and uses `mule-linter-core` for versioning. Release state lives in module `version.properties` files and `jreleaser.yml`.
- `config/code-quality-config` is a Git submodule. If needed, initialize the checked-in revision with `git submodule update --init --recursive`, not the README's `--remote` command, which advances it.
