# CLI Output Format Options - Table-Based with Colors

## Option A: Clean Minimal Table

Simple, compact table with minimal decoration:

```
═══════════════════════════════════════════════════════════════════════════════
                        Mule Linter Report
═══════════════════════════════════════════════════════════════════════════════
Rules: 36 executed │ Violations: 5 🔴  12 🟡  25 🔵
═══════════════════════════════════════════════════════════════════════════════

🔴 CRITICAL (5)
───────────────────────────────────────────────────────────────────────────────
Rule                          Location                    Message
───────────────────────────────────────────────────────────────────────────────
AZURE_PIPELINES_EXISTS        azure-pipelines.yml         File does not exist
JENKINS_EXISTS                Jenkinsfile                 File does not exist
ENCRYPTED_VALUE               prod.properties:12          Property 'db.password'
                                                          is not encrypted
MULE_RUNTIME                  pom.xml                     Version mismatch
POM_PLUGIN_ATTRIBUTE          pom.xml                     Missing extensions
───────────────────────────────────────────────────────────────────────────────

🟡 MAJOR (12)
───────────────────────────────────────────────────────────────────────────────
Rule                          Location                    Message
───────────────────────────────────────────────────────────────────────────────
UNUSED_FLOW                   business-logic.xml:34       Flow not referenced
UNUSED_FLOW                   business-logic.xml:56       Flow not referenced
GIT_IGNORE                    .gitignore:1                Missing: target/
GIT_IGNORE                    .gitignore:1                Missing: .idea/
LOGGER_MESSAGE_CONTENTS       sample.xml:21               Invalid pattern
───────────────────────────────────────────────────────────────────────────────

🔵 MINOR (25) - Non-failing
───────────────────────────────────────────────────────────────────────────────
LOGGER_CATEGORY_HASVALUE      sample.xml:21               Missing category
LOGGER_ATTRIBUTES_RULE      sample.xml:23               Missing attribute
───────────────────────────────────────────────────────────────────────────────

═══════════════════════════════════════════════════════════════════════════════
Exit Code: 1 │ Failed: 17 │ Non-failing: 25
═══════════════════════════════════════════════════════════════════════════════
```

**Characteristics:**
- Thin separator lines (─)
- Simple column alignment
- "Non-failing" label for MINOR section
- Summary with exit code and counts

---

## Option B: Box-Style Table with Grid

Full box drawing characters for clear visual boundaries:

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                         Mule Linter Report                                    ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃  Rules: 36 executed  │  Violations: 5 🔴  12 🟡  25 🔵  │  Exit: 1            ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ 🔴 CRITICAL (5) - Failing                                                    ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃ Rule                        ┃ Location                ┃ Message              ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━━━━━┫
┃ AZURE_PIPELINES_EXISTS      ┃ azure-pipelines.yml       ┃ File does not exist  ┃
┃ JENKINS_EXISTS              ┃ Jenkinsfile               ┃ File does not exist  ┃
┃ ENCRYPTED_VALUE             ┃ prod.properties:12        ┃ Property not encryp… ┃
┃ MULE_RUNTIME                ┃ pom.xml                   ┃ Version mismatch     ┃
┃ POM_PLUGIN_ATTRIBUTE        ┃ pom.xml                   ┃ Missing extensions   ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━┛

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ 🟡 MAJOR (12) - Failing                                                      ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃ Rule                        ┃ Location                ┃ Message              ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━━━━━┫
┃ UNUSED_FLOW                 ┃ business-logic.xml:34   ┃ Flow not referenced  ┃
┃ UNUSED_FLOW                 ┃ business-logic.xml:56   ┃ Flow not referenced  ┃
┃ GIT_IGNORE                  ┃ .gitignore:1              ┃ Missing: target/     ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━┛

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ 🔵 MINOR (25) - Non-failing                                                  ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
(25 violations below MAJOR threshold)

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃ Summary: 17 failing │ 25 non-failing │ Exit Code: 1                            ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

**Characteristics:**
- Full box borders (┏┓┗┛┣┫┃━┳┻╋)
- Each section is its own box
- Clear "Failing" vs "Non-failing" labels
- Minor section collapsed to save space

---

## Option C: Modern Compact Table

Streamlined with strategic use of color and spacing:

```
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
                         MULE LINTER REPORT
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓

Rules: 36 │ Violations: 5 🔴  12 🟡  25 🔵 │ Exit: @|red,bold 1|@

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🔴 CRITICAL                                                                  │
├────────────────┬──────────────────────────┬───────────────────────────────────┤
│ AZURE_PIPELINE │ azure-pipelines.yml      │ File does not exist               │
│ JENKINS_EXISTS │ Jenkinsfile              │ File does not exist               │
│ ENCRYPTED_VALU │ prod.properties:12     │ Property 'db.password' not encryp │
│ MULE_RUNTIME   │ pom.xml                  │ Version 4.9.16 ≠ 4.3.0            │
│ POM_PLUGIN_ATT │ pom.xml                  │ Missing extensions=true           │
└────────────────┴──────────────────────────┴───────────────────────────────────┘
                                    5 total

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🟡 MAJOR                                                                     │
├────────────────┬──────────────────────────┬───────────────────────────────────┤
│ UNUSED_FLOW    │ business-logic.xml:34    │ Flow 'b-sub-flow' not referenced  │
│ UNUSED_FLOW    │ business-logic.xml:56    │ Flow 'a-sub-flow' not referenced  │
│ GIT_IGNORE     │ .gitignore:1             │ Missing: target/                  │
│ GIT_IGNORE     │ .gitignore:1             │ Missing: .idea/                   │
│ GIT_IGNORE     │ .gitignore:1             │ Missing: .classpath/              │
│ LOGGER_MESSAG  │ sample.xml:21            │ Invalid message pattern           │
└────────────────┴──────────────────────────┴───────────────────────────────────┘
                                    12 total

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🔵 MINOR (Non-failing)                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
25 violations below threshold

▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
Exit: @|red,bold 1|@ │ Failing: @|red 17|@ │ Non-failing: @|cyan 25|@
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
```

**Characteristics:**
- Block headers with ▓ characters (attention-grabbing)
- Minimal vertical borders (cleaner look)
- Total count per section
- Collapsed MINOR section with count only
- Color-coded exit code in header and footer

---

## Comparison Matrix

| Feature | Option A: Clean | Option B: Box | Option C: Modern |
|---------|-----------------|---------------|------------------|
| **Visual Weight** | Light | Heavy | Medium |
| **CI Log Friendly** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Scannability** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Row Density** | 3 lines/violation | 1 line/violation | 1 line/violation |
| **No-Color Fallback** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Implementation** | Simple | Medium | Medium |
| **Line Wrap Handling** | Good | Poor (breaks table) | Good |
| **Compactness** | 60 lines | 45 lines | 35 lines |

---

## My Recommendation: Option C (Modern Compact)

**Why:**
1. **Most compact** - Best for CI logs (35 lines vs 60)
2. **Clear hierarchy** - Block headers separate severity levels
3. **Good no-color fallback** - Emoji icons work without ANSI
4. **Efficient** - Collapses MINOR section (just count)
5. **Professional** - Modern appearance without being heavy

**Second choice:** Option A (Clean) if you prefer lighter visual weight.

**Avoid:** Option B in CI environments - box characters create alignment issues when logs are wrapped or processed.

---

## Exit Code & Threshold Implementation

### Exit Code Logic

```java
// Exit codes
static final int EXIT_SUCCESS = 0;        // No violations or only MINOR
static final int EXIT_VIOLATIONS = 1;     // Has CRITICAL or MAJOR
static final int EXIT_CLI_ERROR = 2;      // Picocli handles this
static final int EXIT_FATAL = 3;          // Exceptions

// Threshold check
int calculateExitCode(List<RuleViolation> violations, Severity threshold) {
    boolean hasFailing = violations.stream()
        .anyMatch(v -> v.rule.severity.ordinal() >= threshold.ordinal());
    return hasFailing ? EXIT_VIOLATIONS : EXIT_SUCCESS;
}
```

### Default Threshold: MAJOR

```bash
# Default (--fail-threshold=MAJOR)
./mule-linter --dir app --rules rules.groovy
# Has only MINOR → Exit 0
# Has MAJOR or CRITICAL → Exit 1

# Override
./mule-linter --dir app --rules rules.groovy --fail-threshold=CRITICAL
# Has only MINOR or MAJOR → Exit 0
# Has CRITICAL → Exit 1

./mule-linter --dir app --rules rules.groovy --fail-threshold=MINOR
# Has any violation → Exit 1
```
