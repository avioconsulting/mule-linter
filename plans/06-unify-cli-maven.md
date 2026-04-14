# Plan: Unify CLI and Maven Plugin Architecture

## Goal

Create a unified architecture where both CLI and Maven plugin share:
1. **Common formatting infrastructure** (table format for console, pluggable formatters for files)
2. **Shared configuration options** (thresholds, colors, output formats)
3. **Ability to output multiple formats simultaneously** (like Maven plugin currently does)
4. **Consistent exit/failure behavior**

## Current State Analysis

### CLI (New Implementation)
- ✅ Table format with ANSI colors
- ✅ `--fail-threshold` parameter
- ✅ `--color` / `--no-color` flags
- ❌ Only one output format per run (CONSOLE, JSON, or XML)
- ❌ Always writes to System.out

### Maven Plugin (Legacy Implementation)
- ❌ Old list format (no table, no colors)
- ❌ Hardcoded failure logic (non-MINOR = fail)
- ✅ Multiple formats per run (e.g., `CONSOLE,JSON`)
- ✅ File output support (writes to `${project.build.directory}/mule-linter/`)
- ❌ Doesn't use new CLI features

## Proposed Unified Architecture

### 1. Core Formatter Infrastructure (New Module or Core)

Create a shared formatter system in `mule-linter-core`:

```
mule-linter-core/src/main/groovy/com/avioconsulting/mule/linter/formatter/
├── IReportFormatter.java (interface)
├── ReportFormat.java (enum: CONSOLE, JSON, XML, HTML?)
├── ConsoleTableFormatter.groovy (the new table format)
├── JsonFormatter.groovy (existing JSON logic, moved from RuleExecutor)
├── XmlFormatter.groovy (existing XML logic, moved from RuleExecutor)
└── CompositeFormatter.groovy (handles multiple formats)
```

**Key Design:**
- Formatters write to `OutputStream` or `File` (not just System.out)
- Formatters receive `RuleExecutor` and `FormatterContext` (threshold, color preferences, etc.)
- Console formatter has special handling for ANSI colors
- File-based formatters don't use colors

### 2. Shared Configuration Model

Create a configuration class that works for both CLI and Maven:

```groovy
class LinterConfiguration {
    File appDirectory
    File rulesFile
    RuleSeverity failThreshold = RuleSeverity.MAJOR
    boolean useColor = true
    boolean failBuild = false  // For Maven, always true for CLI
    List<ReportFormat> outputFormats = [ReportFormat.CONSOLE]
    File outputDirectory  // For file-based formats
    
    // Factory methods
    static LinterConfiguration fromCli(MuleLinterCli cli)
    static LinterConfiguration fromMavenMojo(AbstractMuleLinterMojo mojo)
}
```

### 3. Unified MuleLinter API

Simplify MuleLinter to just execute and return results:

```groovy
class MuleLinter {
    RuleExecutor execute() {
        // Just execute rules and return executor with results
        // No output formatting, no exit code calculation
    }
}
```

**Formatting and exit codes move OUT of MuleLinter** and into the wrappers.

### 4. CLI Changes

Support multiple formats and file output:

```bash
# Multiple formats (like Maven)
./mule-linter --dir app --rules rules.groovy --format CONSOLE,JSON,XML

# File output
./mule-linter --dir app --rules rules.groovy --format JSON --output-dir ./reports

# Mixed (console + file)
./mule-linter --dir app --rules rules.groovy --format CONSOLE,JSON --output-dir ./reports
```

**CLI code becomes:**
```java
public Integer call() {
    LinterConfiguration config = LinterConfiguration.fromCli(this);
    
    // Execute
    MuleLinter linter = new MuleLinter(config.appDirectory, config.rulesFile);
    RuleExecutor executor = linter.execute();
    
    // Format results
    CompositeFormatter formatter = new CompositeFormatter(config);
    formatter.format(executor);
    
    // Calculate and return exit code
    return calculateExitCode(executor, config.failThreshold);
}
```

### 5. Maven Plugin Changes

Maven plugin becomes a thin wrapper:

```java
public void execute() {
    LinterConfiguration config = LinterConfiguration.fromMavenMojo(this);
    
    // Execute
    MuleLinter linter = new MuleLinter(config.appDirectory, config.rulesFile);
    RuleExecutor executor = linter.execute();
    
    // Format results
    CompositeFormatter formatter = new CompositeFormatter(config);
    formatter.format(executor);
    
    // Fail if needed
    if (config.failBuild) {
        failIfNeeded(executor, config.failThreshold);
    }
}
```

**Maven gets new features:**
- Table format for console output
- `<failThreshold>` parameter
- `<useColor>` parameter (maybe? Or respect Maven's batch mode)

### 6. Shared Exit/Failure Logic

Move exit code calculation to a shared utility:

```groovy
class LinterResultEvaluator {
    static boolean hasViolationsAtOrAboveThreshold(List<RuleViolation> violations, RuleSeverity threshold) {
        return violations.any { it.rule.severity.ordinal() <= threshold.ordinal() }
    }
    
    static int calculateExitCode(List<RuleViolation> violations, RuleSeverity threshold) {
        return hasViolationsAtOrAboveThreshold(violations, threshold) ? 1 : 0
    }
}
```

Both CLI and Maven plugin use this.

## Open Questions

### Q1: Where should formatters live?
**Option A:** Keep in `mule-linter-core` (recommended)
- Pro: Shared code, no new module
- Con: Core module grows

**Option B:** New `mule-linter-formatters` module
- Pro: Clean separation
- Con: More modules to maintain

### Q2: Maven output directory handling
Currently Maven writes to `${project.build.directory}/mule-linter/`. 
Should CLI also have a default output directory, or require explicit `--output-dir`?

### Q3: Color support in Maven
Maven has `maven.color.disabled` property. Should we:
- Respect Maven's color setting?
- Add `<useColor>` parameter?
- Always disable colors in Maven (use log levels instead)?

### Q4: Console vs File output for multiple formats
When user specifies `--format CONSOLE,JSON,XML`:
- CONSOLE → System.out (with colors if TTY)
- JSON → File (no colors)
- XML → File (no colors)

What should the filenames be? `mule-linter-report.json`, `mule-linter-report.xml`?

### Q5: Backward compatibility
CLI currently has `--format` with single value. Changing to multi-value is a breaking change.
Should we:
- Add new `--formats` (plural) and deprecate `--format`?
- Keep `--format` but allow comma-separated values?
- Break compatibility (we're on feature branch, acceptable)?

### Q6: HTML format?
Should we add HTML report format while we're refactoring? Could be useful for CI artifacts.

## Deliverables

### Phase 1: Core Infrastructure
- [ ] Create `IReportFormatter` interface
- [ ] Create `FormatterContext` configuration class
- [ ] Move JSON/XML formatters from RuleExecutor to new structure
- [ ] Create `ConsoleTableFormatter` (adapt from current RuleExecutor)
- [ ] Create `CompositeFormatter` for multiple formats
- [ ] Create `LinterConfiguration` shared config class

### Phase 2: CLI Updates
- [ ] Update CLI to support multiple formats
- [ ] Add `--output-dir` parameter
- [ ] Refactor CLI to use new formatter infrastructure
- [ ] Maintain backward compatibility (or handle breaking change)

### Phase 3: Maven Plugin Updates
- [ ] Add `<failThreshold>` parameter
- [ ] Add `<useColor>` parameter (optional)
- [ ] Refactor Maven plugin to use new formatter infrastructure
- [ ] Remove old FormatterBuilder/IFormatter system
- [ ] Ensure table format works with Maven log output

### Phase 4: Testing
- [ ] Test multiple formats in CLI
- [ ] Test file output
- [ ] Test Maven plugin with new features
- [ ] Test backward compatibility
- [ ] Test edge cases (no violations, many violations, etc.)

## Implementation Strategy

1. **Start with Phase 1** - Create formatter infrastructure
2. **Keep existing code working** - Don't break CLI or Maven during transition
3. **Migrate CLI first** - It's simpler, fewer dependencies
4. **Migrate Maven plugin** - Update to use new infrastructure
5. **Remove old code** - Delete RuleExecutor.displayResults(), old Maven formatters

## Risks

| Risk | Mitigation |
|------|------------|
| Breaking changes to CLI API | Document in release notes; we're on feature branch |
| Maven plugin behavior changes | Thorough testing; document in release notes |
| Increased complexity | Good abstraction layers; clear separation of concerns |
| Performance with multiple formats | Formatters write independently; minimal overhead |

## Success Criteria

- [ ] Both CLI and Maven plugin can output CONSOLE + JSON + XML simultaneously
- [ ] Both use the same table format for console output
- [ ] Both respect `--fail-threshold` / `<failThreshold>`
- [ ] Both use shared configuration options
- [ ] File output works for JSON/XML in both
- [ ] All existing tests pass
- [ ] New tests for multiple format output

---

**Awaiting your answers to the Open Questions before proceeding with implementation.**
