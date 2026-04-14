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

## Current Status (Updated April 14, 2026)

### ✅ Completed

**Phase 1: SPI Core Classes Migrated to Java**

1. **RuleSeverity** - Converted from Groovy enum to Java enum
   - File: `mule-linter-spi/src/main/java/.../RuleSeverity.java`
   - Removed: `RuleSeverity.groovy`

2. **RuleType** - Converted from Groovy enum to Java enum
   - File: `mule-linter-spi/src/main/java/.../RuleType.java`
   - Removed: `RuleType.groovy`

3. **Rule** - Converted from Groovy abstract class to Java abstract class
   - File: `mule-linter-spi/src/main/java/.../Rule.java`
   - Preserved: All constructors, getters/setters, `init()` method, `execute()` abstract method
   - Removed: `Rule.groovy`

4. **RuleViolation** - Converted from Groovy class to Java class
   - File: `mule-linter-spi/src/main/java/.../RuleViolation.java`
   - Preserved: All fields, constructors, getters/setters, `toString()`
   - Removed: `RuleViolation.groovy`

5. **RuleProvider** - Created new Java interface
   - File: `mule-linter-spi/src/main/java/.../RuleProvider.java`
   - Methods: `getProviderName()`, `getRules()`

6. **RuleDescriptor** - Created new Java class
   - File: `mule-linter-spi/src/main/java/.../RuleDescriptor.java`
   - Fields: ruleId, displayName, description, defaultSeverity, ruleType, factory
   - Methods: getters, `createRule()`

7. **RuleRegistry** - Created new Groovy class to replace RulesLoader
   - File: `mule-linter-core/src/main/groovy/.../RuleRegistry.groovy`
   - Features: ServiceLoader support, explicit registration, singleton pattern
   - Note: Still uses Groovy for DSL compatibility

### 🚧 In Progress / Remaining Work

**Phase 2: BuiltInRuleProvider Implementation**

- Need to create `BuiltInRuleProvider` class in core
- Must explicitly register all 49 built-in rules with their descriptors
- Challenge: Each rule needs a factory lambda and metadata

**Phase 3: Update DSL and Remove RulesLoader**

- Update `RulesDsl.methodMissing()` to use `RuleRegistry` instead of `RulesLoader`
- Remove `RulesLoader.groovy` entirely
- Update `MuleLinter` to use new registry

**Phase 4: Test and Verify**

- All existing tests must pass
- DSL configurations must work unchanged
- External rule loading via ServiceLoader must work

## Open Questions

1. **BuiltInRuleProvider Implementation Strategy**
   - Should we manually create descriptors for all 49 rules, or generate them?
   - How do we handle rules that have different default severities/types?

2. **Rule Class Compatibility**
   - Do existing Groovy rule classes need changes to extend the Java Rule class?
   - The Java Rule has protected constructors - will Groovy rules work with `newInstance()`?

3. **DSL Compatibility**
   - The DSL uses `methodMissing` to instantiate rules - will this work with RuleDescriptor factories?
   - How do we handle rule configuration closures with the new factory pattern?

4. **ServiceLoader Registration**
   - Where should `META-INF/services/com.avioconsulting.mule.linter.model.rule.RuleProvider` files be created?
   - Should external providers override built-in rules with the same ID?

5. **Testing Strategy**
   - How do we test the registry without initializing all 49 rules?
   - Should we add a test-only rule provider?

## Current Problems (Original)

1. Public SPI is implemented in Groovy and uses dynamic behavior in several places.
2. `RulesLoader` scans the world using `Reflections` and package heuristics.
3. External rule loading is not designed as a first-class SPI.
4. `ComponentsFactory.registeredComponents()` is present but not meaningfully used.
5. Dynamic rule discovery makes startup and maintenance less predictable.

## Desired End State

### SPI Responsibilities

Keep SPI as the home for:

- `Application` (still Groovy - complex model)
- ✅ `Rule` (now Java)
- ✅ `RuleViolation` (now Java)
- ✅ `RuleSeverity` (now Java)
- ✅ `RuleType` (now Java)
- `Param` (still Groovy)
- `MuleComponent` and typed component contracts (still Groovy)
- `PomFile`-related public abstractions (still Groovy)
- Component extension interfaces (still Groovy)
- ✅ New `RuleProvider` interface (Java)
- ✅ New `RuleDescriptor` class (Java)

### New Rule Loading Model

Replace reflection scanning with explicit registration via `RuleRegistry`:

```java
// Built-in provider loads on startup
RuleRegistry.initialize() // loads BuiltInRuleProvider + ServiceLoader providers

// DSL resolves rules via registry
RuleDescriptor desc = RuleRegistry.getRuleDescriptor("AZURE_PIPELINES_EXISTS")
Rule rule = desc.createRule()
```

## Proposed Architecture (Implemented)

### ✅ Option B: Provider Returns Descriptors (Implemented)

**SPI Classes:**
- `RuleProvider` interface with `getProviderName()` and `getRules()`
- `RuleDescriptor` with id, display name, description, severity, type, and factory

**Core Classes:**
- `RuleRegistry` singleton that aggregates all providers
- `BuiltInRuleProvider` (needs implementation) for all built-in rules
- ServiceLoader support for external providers

### Remaining Work Details

**Create BuiltInRuleProvider:**

```groovy
class BuiltInRuleProvider implements RuleProvider {
    List<RuleDescriptor> getRules() {
        return [
            new RuleDescriptor(
                "AZURE_PIPELINES_EXISTS",
                "Azure Pipelines Exists",
                "Checks for azure-pipelines.yml file",
                RuleSeverity.CRITICAL,
                RuleType.CODE_SMELL,
                { -> new AzurePipelinesExistsRule() }
            ),
            // ... 48 more rules
        ]
    }
}
```

**Update RulesDsl:**

```groovy
def methodMissing(String name, args) {
    def descriptor = RuleRegistry.getRuleDescriptor(name)
    if (descriptor) {
        def rule = descriptor.createRule()
        // ... configure rule
        ruleSet.addRule(rule)
    }
}
```

**Remove RulesLoader:**
- Delete `RulesLoader.groovy`
- Remove `org.reflections` dependency from core

## Verification Strategy

Must verify:

- ✅ Java SPI classes compile
- 🔄 built-in DSL config loads successfully (pending)
- 🔄 representative built-in rules execute (pending)
- 🔄 external component extension from `mule-linter-spi-test` still works (pending)
- 🔄 add or adapt an external rule-provider test (pending)

Commands:

```bash
./gradlew :mule-linter-spi:compileJava  # ✅ Passes
./gradlew :mule-linter-core:test       # 🔄 Needs BuiltInRuleProvider
./gradlew test                          # 🔄 Pending
```

## Risks (Updated)

| Risk | Status | Mitigation |
|------|--------|------------|
| Groovy rules may depend on dynamic property semantics from SPI classes | ⚠️ Watch | Java Rule has getters/setters; Groovy should still work |
| Java migration may expose weakly-defined parts of the current public API | ✅ Addressed | Explicit Java types now defined |
| Extension examples may need changes to service registration | 🔄 Pending | Document ServiceLoader usage |
| Mixed Java/Groovy compilation order can cause temporary friction | ✅ Addressed | SPI compiles first, core depends on it |
| BuiltInRuleProvider may be large and hard to maintain | 🔄 Open | Consider code generation or convention-based approach |

## Recommended Sequence (Updated)

1. ✅ Freeze current public SPI shape.
2. ✅ Move core SPI contracts to Java (Rule, RuleViolation, RuleSeverity, RuleType).
3. ✅ Add `RuleProvider` SPI and `RuleDescriptor`.
4. ✅ Create `RuleRegistry` to replace RulesLoader.
5. 🔄 **Current:** Create `BuiltInRuleProvider` with all 49 rules.
6. 🔄 Update `RulesDsl` to use registry.
7. 🔄 Remove `RulesLoader` and `Reflections` dependency.
8. 🔄 Update tests and extension example.
9. 🔄 Verify all existing DSL configs still work.

## Deliverables (Partial)

- ✅ Java-based SPI core classes (Rule, RuleViolation, enums)
- ✅ `RuleProvider` interface
- ✅ `RuleDescriptor` class
- ✅ `RuleRegistry` implementation
- 🔄 explicit built-in rule registry/provider (in progress)
- external rule provider SPI and test coverage (pending)
- preserved Groovy DSL experience (pending verification)
- removal of `Reflections` from runtime rule loading (pending)

## Next Steps

1. **Decision needed:** How to implement BuiltInRuleProvider efficiently?
   - Option A: Manual registration of all 49 rules (explicit, verbose)
   - Option B: Convention-based (e.g., scan classpath once at build time, generate provider)
   - Option C: Keep reflection but only for BuiltInRuleProvider initialization

2. Once BuiltInRuleProvider is created:
   - Update RulesDsl to use RuleRegistry
   - Remove RulesLoader
   - Run tests
   - Commit and push

## Branch

Current work is on branch: `feat/spi-java`
Base branch: `feat/unify-formatters` (contains unified formatter infrastructure)
