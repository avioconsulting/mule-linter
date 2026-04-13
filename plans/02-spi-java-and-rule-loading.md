# Plan: Migrate SPI to Java and Redesign Rule Loading

## Goal

Move the stable SPI surface from Groovy to Java, remove fragile reflection-based rule discovery, and introduce a proper extensibility model for rules while preserving the current Groovy rule configuration DSL.

## Why This Matters

- The SPI is the public contract for rule authors and extension libraries.
- Java is a better fit for stable APIs, explicit types, and agent-assisted maintenance.
- Current rule loading is implicit and brittle.
- Keeping the Groovy DSL is compatible with a Java-backed rule registry.

## Constraints

- Keep the existing Groovy rule configuration DSL experience.
- Preserve current built-in rule IDs.
- Preserve external component extension support.
- Avoid a full rewrite of all built-in rules in the first pass.

## Success Criteria

- `mule-linter-spi` is Java-based or predominantly Java-based for all public contracts.
- Rule discovery no longer depends on broad package scanning with `Reflections`.
- Core can load built-in rules explicitly and external rules through a well-defined provider SPI.
- Existing DSL configuration files continue to work with the same rule IDs.
- SPI-test or an equivalent extension test proves external component and rule loading.

## Current Problems

1. Public SPI is implemented in Groovy and uses dynamic behavior in several places.
2. `RulesLoader` scans the world using `Reflections` and package heuristics.
3. External rule loading is not designed as a first-class SPI.
4. `ComponentsFactory.registeredComponents()` is present but not meaningfully used.
5. Dynamic rule discovery makes startup and maintenance less predictable.

## Desired End State

### SPI Responsibilities

Keep SPI as the home for:

- `Application`
- `Rule`
- `RuleViolation`
- `RuleSeverity`
- `RuleType`
- `Param`
- `MuleComponent` and typed component contracts
- `PomFile`-related public abstractions as appropriate
- Component extension interfaces
- New rule extension interfaces

### New Rule Loading Model

Replace reflection scanning with explicit registration.

Recommended additions:

- `RuleProvider` interface in SPI
- `RuleDescriptor` or equivalent metadata object
- Built-in provider in core for all built-in rules
- External providers loaded via `ServiceLoader`

This keeps the Groovy DSL while changing only how `RULE_ID -> rule class/factory` is resolved.

## Proposed Architecture

### Option A: Provider Returns Rule Instances

Define in SPI:

- `interface RuleProvider { Map<String, Supplier<? extends Rule>> rules(); }`

Pros:
- simple
- explicit
- no reflection scanning

Cons:
- metadata may still live on rule classes unless duplicated

### Option B: Provider Returns Descriptors

Define in SPI:

- `RuleDescriptor` with id, display name, description, and factory

Pros:
- better future tooling
- easier code completion/doc generation

Cons:
- a little more code

Recommendation: use Option B if doing the migration anyway.

## Phase 1: Migrate SPI Surface to Java

Convert first:

- `Application`
- rule classes and enums in `model/rule`
- `ComponentIdentifier`
- `MuleComponent`, `FlowComponent`, `LoggerComponent`, `AVIOLoggerComponent`
- `ComponentsFactory`
- basic file model classes that are truly public API

Guidelines:

- preserve package names where possible
- preserve public method names where practical
- reduce Groovy metaprogramming in public types
- prefer explicit getters over dynamic property fallbacks in public API

## Phase 2: Add Proper Rule SPI

1. Introduce `RuleProvider` in SPI.
2. Add a built-in provider in core that explicitly registers all built-in rules.
3. Replace `RulesLoader` package scanning with provider aggregation.
4. Load providers via:
   - built-in explicit registration
   - `ServiceLoader<RuleProvider>` for external libraries
5. Keep `RulesDsl.methodMissing(...)` but have it consult the registry instead of `Reflections`.

## Phase 3: Revisit Component Extension SPI

1. Keep `ComponentsFactory`, but simplify or tighten it.
2. Either remove `registeredComponents()` if unnecessary or make it the primary lookup source.
3. Make component resolution explicit and deterministic.
4. Decide whether nested components should also be factory-resolved rather than always generic.

## Phase 4: Compatibility Layer

To reduce disruption:

- Keep existing `RULE_ID` constants on rules.
- Keep DSL names mapped exactly to those IDs.
- Keep current built-in rule classes usable while infrastructure changes underneath.
- Do not require all rules to move to Java in this phase.

## Verification Strategy

Must verify:

- built-in DSL config loads successfully
- representative built-in rules execute
- external component extension from `mule-linter-spi-test` still works
- add or adapt an external rule-provider test to prove external rule loading

Commands:

- `./gradlew :mule-linter-spi:test`
- `./gradlew :mule-linter-core:test`
- `./gradlew :mule-linter-spi-test:test`
- `./gradlew test`

## Risks

- Groovy rules may depend on dynamic property semantics from SPI classes.
- Java migration may expose weakly-defined parts of the current public API.
- Extension examples may need changes to service registration.
- Mixed Java/Groovy compilation order can cause temporary friction during migration.

## Recommended Sequence

1. Freeze current public SPI shape.
2. Move SPI public contracts to Java.
3. Add `RuleProvider` SPI.
4. Replace `RulesLoader` implementation.
5. Update tests and extension example.
6. Only then consider migrating selected built-in rules to Java if beneficial.

## Deliverables

- Java-based SPI public API
- explicit built-in rule registry/provider
- external rule provider SPI and test coverage
- preserved Groovy DSL experience
- removal of `Reflections` from runtime rule loading
