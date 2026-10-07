package com.avioconsulting.mule.linter.model.rule

/** Shared explicit severity ordering; independent of enum declaration order. */
final class FailurePolicy {
    private static final Map<RuleSeverity, Integer> RANK = Collections.unmodifiableMap([
            (RuleSeverity.BLOCKER): 4, (RuleSeverity.CRITICAL): 3,
            (RuleSeverity.MAJOR): 2, (RuleSeverity.MINOR): 1])

    static boolean fails(AnalysisResult result, boolean enforce, RuleSeverity threshold = RuleSeverity.MAJOR) {
        if (threshold == null) throw new IllegalArgumentException('Failure threshold is required')
        enforce && result.findings.any { meetsThreshold(it.severity, threshold) }
    }

    static boolean meetsThreshold(RuleSeverity severity, RuleSeverity threshold) {
        if (severity == null || threshold == null) throw new IllegalArgumentException('Severity and threshold are required')
        RANK[severity] >= RANK[threshold]
    }

    static void requireComplete(AnalysisResult result, boolean strict) {
        if (strict && !result.complete) throw new IllegalStateException('Analysis incomplete: ' + result.warnings.join('; '))
    }
}
