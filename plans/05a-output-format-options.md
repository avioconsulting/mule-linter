# CLI Output Format Options

## Option 1: Enhanced List with Colors (Minimal Change)

Most similar to current output, but with colors and better formatting:

```
Mule Linter Report
═══════════════════════════════════════════════════════════════════════════════

36 rules executed | 42 violations found

🔴 CRITICAL (5 violations)
─────────────────────────────────────────────────────────────────────────────────
  [CRITICAL] AZURE_PIPELINES_EXISTS 
             azure-pipelines.yml file does not exist

  [CRITICAL] JENKINS_EXISTS 
             Jenkinsfile file does not exist
             /home/project/Jenkinsfile

  [CRITICAL] ENCRYPTED_VALUE 
             src/main/resources/properties/prod.properties:12
             Property 'db.password' is not encrypted

🟡 MAJOR (12 violations)
─────────────────────────────────────────────────────────────────────────────────
  [MAJOR] UNUSED_FLOW 
          src/main/mule/business-logic.xml:34
          Flow 'b-sub-flow' is not referenced by any flow-ref

  [MAJOR] GIT_IGNORE 
          .gitignore:1
          Missing required expression: target/

🔵 MINOR (25 violations)
─────────────────────────────────────────────────────────────────────────────────
  [MINOR] LOGGER_CATEGORY_HASVALUE 
          src/main/mule/sample.xml:21
          Logger missing category attribute

═══════════════════════════════════════════════════════════════════════════════
Summary: 5 CRITICAL, 12 MAJOR, 25 MINOR
═══════════════════════════════════════════════════════════════════════════════
```

### Pros
- Familiar format, easy transition from current output
- Colors make severity immediately visible
- Line numbers and file paths are clearly associated
- Summary at bottom

### Cons
- Still linear, can be hard to scan many violations
- File paths can be long and wrap awkwardly
- No visual grouping by file

---

## Option 2: Compact Table Format

Uses ASCII table borders for alignment:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Mule Linter Report                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  36 rules executed  │  42 violations found  │  Exit Code: 1                │
└─────────────────────────────────────────────────────────────────────────────┘

🔴 CRITICAL (5)
┌────────────────────┬──────────────────────────────────────┬──────────────────┐
│ Rule               │ Location                             │ Message          │
├────────────────────┼──────────────────────────────────────┼──────────────────┤
│ AZURE_PIPELINES_E… │ azure-pipelines.yml                  │ File does not ex…│
│ JENKINS_EXISTS     │ Jenkinsfile                          │ File does not ex…│
│ ENCRYPTED_VALUE    │ …/prod.properties:12                 │ Property 'db.pa…│
│ MULE_RUNTIME       │ pom.xml                              │ Version 4.9.16 …│
│ POM_PLUGIN_ATTRI…  │ pom.xml                              │ extensions=true… │
└────────────────────┴──────────────────────────────────────┴──────────────────┘

🟡 MAJOR (12)
┌────────────────────┬──────────────────────────────────────┬──────────────────┐
│ Rule               │ Location                             │ Message          │
├────────────────────┼──────────────────────────────────────┼──────────────────┤
│ UNUSED_FLOW        │ business-logic.xml:34                │ Flow 'b-sub-flo…│
│ GIT_IGNORE         │ .gitignore:1                         │ Missing: target/ │
│ GIT_IGNORE         │ .gitignore:1                         │ Missing: .idea/  │
│ LOGGER_MESSAGE_CO… │ sample.xml:21                        │ Invalid message… │
└────────────────────┴──────────────────────────────────────┴──────────────────┘

🔵 MINOR (25)
┌────────────────────┬──────────────────────────────────────┬──────────────────┐
│ Rule               │ Location                             │ Message          │
├────────────────────┼──────────────────────────────────────┼──────────────────┤
│ LOGGER_CATEGORY_…  │ sample.xml:21                        │ Missing category │
│ … (23 more)        │ …                                    │ …                │
└────────────────────┴──────────────────────────────────────┴──────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ Total: 5 CRITICAL │ 12 MAJOR │ 25 MINOR                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Pros
- Excellent alignment and scannability
- Truncates long text gracefully with ellipsis
- Professional appearance
- Grouped by severity

### Cons
- ASCII tables can break with very long paths
- More complex to implement (column width calculations)
- Can feel cramped in narrow terminals
- Colors work but table borders add noise

---

## Option 3: Grouped by File with Severity Icons

Organizes violations by file location (like compiler output):

```
================================================================================
                           Mule Linter Report
================================================================================
Rules: 36 executed | Violations: 5 🔴  12 🟡  25 🔵 (Total: 42)
================================================================================

🔴 CRITICAL
--------------------------------------------------------------------------------

azure-pipelines.yml
  🔴 [CRITICAL] AZURE_PIPELINES_EXISTS: File does not exist

Jenkinsfile
  🔴 [CRITICAL] JENKINS_EXISTS: File does not exist

src/main/resources/properties/prod.properties
  12 | 🔴 [CRITICAL] ENCRYPTED_VALUE: Property 'db.password' is not encrypted

pom.xml
  45 | 🔴 [CRITICAL] MULE_RUNTIME: Version 4.9.16 does not match expected 4.3.0
  67 | 🔴 [CRITICAL] POM_PLUGIN_ATTRIBUTE: extensions=true required

🟡 MAJOR
--------------------------------------------------------------------------------

src/main/mule/business-logic.xml
  34 | 🟡 [MAJOR] UNUSED_FLOW: Flow 'b-sub-flow' not referenced
  56 | 🟡 [MAJOR] UNUSED_FLOW: Flow 'a-sub-flow' not referenced

.gitignore
   1 | 🟡 [MAJOR] GIT_IGNORE: Missing required: target/
   1 | 🟡 [MAJOR] GIT_IGNORE: Missing required: .idea/
   1 | 🟡 [MAJOR] GIT_IGNORE: Missing required: .classpath/

src/main/mule/sample.xml
  21 | 🟡 [MAJOR] LOGGER_MESSAGE_CONTENTS: Message violates pattern [0-9]*

🔵 MINOR
--------------------------------------------------------------------------------

src/main/mule/sample.xml
  21 | 🔵 [MINOR] LOGGER_CATEGORY_HASVALUE: Logger missing category
  23 | 🔵 [MINOR] LOGGER_ATTRIBUTES_RULE: Missing required attribute

================================================================================
                         Summary: 42 violations found
================================================================================
```

### Pros
- Groups by file - developers fix one file at a time
- Shows line numbers inline (IDE-like format)
- Easy to correlate with IDE/editor navigation
- Severity icons work without ANSI colors
- Natural workflow: open file, go to line, fix issues

### Cons
- Same rule violations scattered across different files
- Takes more vertical space
- No quick way to see "all CRITICAL issues"

---

## Comparison Matrix

| Feature | Option 1: Enhanced List | Option 2: Table | Option 3: By File |
|---------|-------------------------|-----------------|-------------------|
| **Familiarity** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Scannability** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **CI/Log Friendly** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **IDE Navigation** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Implementation** | ⭐⭐⭐⭐⭐ Easy | ⭐⭐ Complex | ⭐⭐⭐⭐ Straightforward |
| **Color Support** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **No-Color Fallback** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ Best |

---

## Recommendations

### For Immediate Implementation (Exit Codes Only)
Start with **Option 0** (current format) + exit codes. No output changes, just:
- Return 0 if no violations
- Return 1 if violations exist

### For Output Improvement
**My recommendation: Option 3 (Grouped by File)**

Reasons:
1. Most actionable - developers work file-by-file
2. Works great with or without colors (icons as fallback)
3. Easy to implement (just sort violations by filename)
4. Familiar format (similar to compiler/javac output)
5. Vertical scrolling is fine in CI logs

Alternative: **Option 1** if you prefer minimal changes.

---

## ANSI Color Implementation Notes

All options use these color mappings:

```java
// Picocli ANSI format strings
String CRITICAL = "@|red,bold 🔴 [CRITICAL]|@";
String MAJOR    = "@|yellow 🟡 [MAJOR]|@";
String MINOR    = "@|cyan 🔵 [MINOR]|@";
String BLOCKER  = "@|red,bold,intense 🛑 [BLOCKER]|@";  // if added later

// Fallback (no color)
String CRITICAL_PLAIN = "[CRITICAL]";
```

Picocli automatically handles:
- Detection of non-TTY output (pipes, files, CI)
- `NO_COLOR=1` environment variable
- `--color` / `--no-color` flags (if we add them)
- Windows CMD compatibility
