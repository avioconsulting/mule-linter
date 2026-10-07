package com.avioconsulting.mule.linter.model.rule

import com.google.gson.Gson
import groovy.transform.Immutable

/** Scalar snapshot: report generation never retains a mutable Rule or violation. */
@Immutable
class AnalysisFinding {
    String ruleId
    String ruleName
    RuleSeverity severity
    RuleType ruleType
    String fileName
    Integer lineNumber
    String message
    // Preserve the legacy XML representation, including extension rule properties.
    String legacyJson
    String legacySerializationError

    static AnalysisFinding snapshot(RuleViolation violation) {
        if (violation.rule == null || violation.rule.severity == null || violation.rule.ruleType == null) {
            throw new IllegalArgumentException('Rule violations must have rule metadata, severity and type')
        }
        String json = null
        String serializationError = null
        try {
            json = new Gson().toJson(violation)
        } catch (Exception e) {
            // Legacy XML serializes extension properties. An unsupported property must
            // fail a requested XML report, not an unrelated console/JSON analysis.
            serializationError = e.message ?: e.class.name
        }
        new AnalysisFinding(violation.rule.ruleId, violation.rule.ruleName,
                violation.rule.severity, violation.rule.ruleType, violation.fileName,
                violation.lineNumber, violation.message, json, serializationError)
    }
}
