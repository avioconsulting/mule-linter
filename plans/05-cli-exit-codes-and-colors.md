# Plan: CLI Exit Codes, Thresholds, and Table Output

## Goal

Implement proper exit codes for CI/CD integration, configurable failure thresholds, and an improved table-based output format with ANSI colors.

## Why This Matters

- **Exit codes enable CI/CD integration** - Currently always returns 0, so builds never fail
- **Thresholds provide flexibility** - MINOR violations shouldn't fail builds by default
- **Table format improves readability** - Clear separation of Rule, Location, Message
- **Colors enhance scannability** - Quick visual identification of severity

## Specifications

### Output Format: Box-Style Table

**Total Width: 130 characters**

Structure:
```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ 🔴 CRITICAL (5) - Failing                                                                                                      │
├──────────────────────────────┬──────────────────────────────┬──────────────────────────────────────────────────────────────┤
│ Rule                         │ Location                     │ Message                                                      │
├──────────────────────────────┼──────────────────────────────┼──────────────────────────────────────────────────────────────┤
│ AZURE_PIPELINES_EXISTS       │ azure-pipelines.yml          │ File does not exist                                          │
│ JENKINS_EXISTS               │ Jenkinsfile                  │ File does not exist                                          │
│ ENCRYPTED_VALUE              │ prod.properties:12           │ Property 'db.password' is not encrypted                        │
│ MULE_RUNTIME                 │ pom.xml                      │ Version 4.9.16 does not match expected 4.3.0                 │
│ POM_PLUGIN_ATTRIBUTE         │ pom.xml                      │ Missing extensions=true attribute                            │
└──────────────────────────────┴──────────────────────────────┴──────────────────────────────────────────────────────────────┘
```

**Column Breakdown:**
- Left border: `│ ` (2 chars)
- Rule: 30 chars content + 1 space padding = 31 total
- Separator: `│ ` (2 chars)
- Location: 30 chars content + 1 space padding = 31 total
- Separator: `│ ` (2 chars)
- Message: 60 chars content + 1 space padding = 61 total
- Right border: `│` (1 char)
- **Total: 2 + 31 + 2 + 31 + 2 + 61 + 1 = 130 characters**

**Truncation Rules:**
- If content exceeds column width, truncate to `width - 1` and add "…" (ellipsis)
- Rule: max 29 chars visible + "…" = 30 total
- Location: max 29 chars visible + "…" = 30 total
- Message: max 59 chars visible + "…" = 60 total

### Sections

1. **Header Box**
   - Title: "Mule Linter Report"
   - Stats: Rules executed, Violation counts by severity, Exit code

2. **CRITICAL Section** (if violations exist)
   - Header: "🔴 CRITICAL (N) - Failing"
   - Full table with all CRITICAL violations
   - Colored emoji + red ANSI text

3. **MAJOR Section** (if violations exist)
   - Header: "🟡 MAJOR (N) - Failing"
   - Full table with all MAJOR violations
   - Colored emoji + yellow/amber ANSI text

4. **MINOR Section** (if violations exist)
   - Header: "🔵 MINOR (N) - Non-failing"
   - Full table with all MINOR violations
   - Colored emoji + cyan ANSI text
   - Labeled as "Non-failing" to indicate threshold behavior

5. **Summary Footer**
   - Total counts: "Summary: X failing (Y 🔴  Z 🟡) │ W non-failing 🔵 │ Exit Code: N"

### Exit Codes

| Code | Meaning | When Returned |
|------|---------|---------------|
| 0 | Success | No violations found, OR only MINOR violations (below threshold) |
| 1 | Violations Found | Has CRITICAL or MAJOR violations (meets or exceeds threshold) |
| 2 | CLI Error | Missing required args, invalid options (handled by picocli) |
| 3 | Fatal Error | Uncaught exceptions, file not found, etc. |

### Threshold Behavior

**Default Threshold: MAJOR**

```bash
# Default behavior (--fail-threshold=MAJOR)
./mule-linter --dir app --rules rules.groovy
# Only MINOR violations → Exit 0 (success)
# Has MAJOR or CRITICAL → Exit 1 (failure)

# Override threshold
./mule-linter --dir app --rules rules.groovy --fail-threshold=CRITICAL
# Only MINOR or MAJOR → Exit 0 (success)
# Has CRITICAL → Exit 1 (failure)

./mule-linter --dir app --rules rules.groovy --fail-threshold=MINOR
# Any violation → Exit 1 (failure)
```

### ANSI Colors

**Severity Colors (picocli format):**
- CRITICAL: `@|red,bold 🔴|@ CRITICAL` - Dark red, bold
- MAJOR: `@|yellow,bold 🟡|@ MAJOR` - Yellow/amber, bold
- MINOR: `@|cyan 🔵|@ MINOR` - Cyan/blue

**Auto-detection:**
- Picocli automatically disables colors when:
  - Output is piped (e.g., `| cat`)
  - `NO_COLOR=1` environment variable is set
  - Terminal doesn't support ANSI
- CLI will add `--color` / `--no-color` flags for manual override

## Files to Modify

### 1. MuleLinterCli.java
**Path:** `mule-linter-cli/src/main/java/com/avioconsulting/mule/linter/MuleLinterCli.java`

**Changes:**
- Add `--fail-threshold` option with enum values (CRITICAL, MAJOR, MINOR)
- Default value: MAJOR
- Pass threshold to MuleLinter constructor
- Accept return value from `runLinter()` and return appropriate exit code
- Add `--color` / `--no-color` options (optional, picocli auto-handles)

```java
@Option(
    names = {"--fail-threshold"},
    description = "Minimum severity that causes non-zero exit code",
    defaultValue = "MAJOR"
)
RuleSeverity failThreshold;

@Override
public Integer call() {
    MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat, failThreshold, useColor);
    int exitCode = ml.runLinter();
    return exitCode;
}
```

### 2. MuleLinter.groovy
**Path:** `mule-linter-core/src/main/groovy/com/avioconsulting/mule/MuleLinter.groovy`

**Changes:**
- Add `RuleSeverity failThreshold` and `boolean useColor` parameters to constructor
- Change `runLinter()` return type from `void` to `int`
- Calculate exit code based on threshold comparison
- Pass color flag to RuleExecutor

```groovy
MuleLinter(File appDir, File rulesFile, ReportFormat format, 
           RuleSeverity failThreshold, boolean useColor) {
    // ... existing init ...
    this.failThreshold = failThreshold
    this.useColor = useColor
}

int runLinter() {
    RuleExecutor exe = buildLinterExecutor()
    exe.displayResults(outputFormat, System.out, useColor)
    return calculateExitCode(exe.results, failThreshold)
}

private int calculateExitCode(List<RuleViolation> violations, RuleSeverity threshold) {
    boolean hasFailing = violations.any { 
        it.rule.severity.ordinal() >= threshold.ordinal() 
    }
    return hasFailing ? 1 : 0
}
```

### 3. RuleExecutor.groovy
**Path:** `mule-linter-core/src/main/groovy/com/avioconsulting/mule/linter/model/rule/RuleExecutor.groovy`

**Changes:**
- Modify `displayResults()` to accept `boolean useColor` parameter
- Implement new table-based CONSOLE format
- Keep existing JSON and XML formats unchanged
- Add helper methods for table formatting

```groovy
void displayResults(ReportFormat format, OutputStream out, boolean useColor) {
    if (format == ReportFormat.CONSOLE) {
        displayTableResults(out, useColor)
    } else {
        // Existing JSON/XML logic unchanged
        displayResults(format, out)
    }
}

private void displayTableResults(OutputStream out, boolean useColor) {
    // Group violations by severity
    def critical = results.findAll { it.rule.severity == RuleSeverity.CRITICAL }
    def major = results.findAll { it.rule.severity == RuleSeverity.MAJOR }
    def minor = results.findAll { it.rule.severity == RuleSeverity.MINOR }
    
    // Write header box
    writeHeaderBox(out, useColor)
    
    // Write each severity section
    if (critical) writeSeveritySection(out, RuleSeverity.CRITICAL, critical, useColor, "Failing")
    if (major) writeSeveritySection(out, RuleSeverity.MAJOR, major, useColor, "Failing")
    if (minor) writeSeveritySection(out, RuleSeverity.MINOR, minor, useColor, "Non-failing")
    
    // Write summary footer
    writeSummaryFooter(out, critical.size(), major.size(), minor.size(), useColor)
}
```

**Table Formatting Helpers:**
```groovy
private String padRight(String s, int width) {
    if (s.length() > width) {
        return s.substring(0, width - 1) + "…"
    }
    return String.format("%-" + width + "s", s)
}

private String formatRow(String rule, String location, String message) {
    "│ " + padRight(rule, 30) + " │ " + 
    padRight(location, 30) + " │ " + 
    padRight(message, 60) + " │"
}

private String colorizeSeverity(RuleSeverity severity, String text, boolean useColor) {
    if (!useColor) return text
    switch(severity) {
        case RuleSeverity.CRITICAL: return "@|red,bold ${text}|@"
        case RuleSeverity.MAJOR: return "@|yellow,bold ${text}|@"
        case RuleSeverity.MINOR: return "@|cyan ${text}|@"
        default: return text
    }
}
```

## Testing Strategy

### Unit Tests (Optional)
Test table formatting logic in isolation:
- Test truncation at exact boundaries
- Test column alignment with various content lengths
- Test color output vs plain output

### Integration Tests
Manual verification commands:
```bash
# Test exit codes
./mule-linter --dir clean-app --rules rules.groovy; echo "Exit: $?"  # Should be 0
./mule-linter --dir violating-app --rules rules.groovy; echo "Exit: $?"  # Should be 1

# Test threshold
./mule-linter --dir app-with-only-minor --rules rules.groovy; echo "Exit: $?"  # Should be 0
./mule-linter --dir app-with-major --rules rules.groovy --fail-threshold=CRITICAL; echo "Exit: $?"  # Should be 0
./mule-linter --dir app-with-minor --rules rules.groovy --fail-threshold=MINOR; echo "Exit: $?"  # Should be 1

# Test colors
./mule-linter --dir app --rules rules.groovy  # Colored if TTY
./mule-linter --dir app --rules rules.groovy | cat  # No colors (piped)
NO_COLOR=1 ./mule-linter --dir app --rules rules.groovy  # No colors (env var)
```

### CI/CD Testing
- Run in actual CI pipeline (GitHub Actions, Jenkins, etc.)
- Verify exit codes work with `set -e` or `|| exit 1` patterns
- Verify table renders correctly in CI log output

## Risks and Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking Maven plugin | Check if Maven plugin calls `runLinter()` - if so, may need overload or update Maven plugin too |
| ANSI codes in CI logs | Picocli auto-detection + `NO_COLOR` support |
| Table wrapping at 130 chars | Document requirement for 130+ char terminal width; content truncates gracefully |
| Performance with many violations | All violations shown, but table format is simple string building - should be fast |
| Backward compatibility | Old behavior (exit 0 always) changes - this is intentional improvement, document in release notes |

## Deliverables

- [ ] Updated `MuleLinterCli.java` with `--fail-threshold` option and exit code handling
- [ ] Updated `MuleLinter.groovy` with threshold logic and exit code calculation
- [ ] Updated `RuleExecutor.groovy` with table-based console output
- [ ] New table formatting utilities (padRight, truncate, colorize)
- [ ] Verification test results (manual testing notes)
- [ ] Updated AGENTS.md with new CLI behavior (if applicable)

## Implementation Order

1. **Phase 1: Exit Codes Only**
   - Add threshold parameter
   - Implement exit code logic
   - Test exit codes work

2. **Phase 2: Table Format**
   - Implement table output in RuleExecutor
   - Add truncation/padding utilities
   - Test with sample data

3. **Phase 3: Colors**
   - Add color parameter
   - Implement colorize helper
   - Test with and without colors

4. **Phase 4: Integration**
   - Wire everything together
   - Full test suite run
   - Manual CLI testing

## Post-Implementation Considerations

- Monitor for user feedback on 130-char width
- Consider adding `--output-width` option if needed
- Consider compact mode for CI logs (collapsing MINOR to summary only)
- Document new CLI options in README

---

**Status:** Ready for review

**Next Step:** Await approval, then proceed with implementation
