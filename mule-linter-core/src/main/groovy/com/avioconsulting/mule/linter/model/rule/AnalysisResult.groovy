package com.avioconsulting.mule.linter.model.rule

/** Immutable result of a single execution. Warnings are not rule violations. */
final class AnalysisResult {
    final List<AnalysisFinding> findings
    final List<String> warnings
    final int ruleCount
    final String applicationPath

    AnalysisResult(List<AnalysisFinding> findings, List<String> warnings, int ruleCount, String applicationPath) {
        this.findings = Collections.unmodifiableList(new ArrayList<>(findings))
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings))
        this.ruleCount = ruleCount
        this.applicationPath = applicationPath
    }

    boolean isComplete() { warnings.isEmpty() }
}
