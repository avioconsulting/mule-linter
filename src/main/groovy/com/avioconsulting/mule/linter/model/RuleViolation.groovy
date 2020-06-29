package com.avioconsulting.mule.linter.model

class RuleViolation {
    Rule rule
    Integer lineNumber
    String fileName
    RuleSeverity severity
    String message

    RuleViolation(Rule rule, String fileName, Integer lineNumber, RuleSeverity severity, String message){
        this.rule = rule
        this.fileName = fileName
        this.lineNumber = lineNumber
        this.severity = severity
        this.message = message
    }

    Rule getRule() {
        return rule
    }

    void setRule(Rule rule) {
        this.rule = rule
    }

    Integer getLineNumber() {
        return lineNumber
    }

    void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber
    }

    String getFileName() {
        return fileName
    }

    void setFileName(String fileName) {
        this.fileName = fileName
    }

    RuleSeverity getSeverity() {
        return severity
    }

    void setSeverity(RuleSeverity severity) {
        this.severity = severity
    }

    String getMessage() {
        return message
    }

    void setMessage(String message) {
        this.message = message
    }

    @Override
    public String toString() {
        return "RuleResult {" +
                "rule = " + rule.getRuleName() +
                ", lineNumber = " + lineNumber +
                ", fileName = '" + fileName + '\'' +
                ", severity = '" + severity + '\'' +
                ", message = '" + message + '\'' +
                '}';
    }
}
