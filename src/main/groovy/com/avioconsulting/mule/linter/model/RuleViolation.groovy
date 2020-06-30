package com.avioconsulting.mule.linter.model

class RuleViolation {

    Rule rule
    Integer lineNumber
    String fileName
    String message

    RuleViolation(Rule rule, String fileName, Integer lineNumber, String message) {
        this.rule = rule
        this.fileName = fileName
        this.lineNumber = lineNumber
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

    String getMessage() {
        return message
    }

    void setMessage(String message) {
        this.message = message
    }

}
