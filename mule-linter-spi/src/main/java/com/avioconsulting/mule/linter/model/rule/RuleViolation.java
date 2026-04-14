package com.avioconsulting.mule.linter.model.rule;

/**
 * Represents a violation found by a rule.
 */
public class RuleViolation {
    
    private Rule rule;
    private Integer lineNumber;
    private String fileName;
    private String message;
    
    public RuleViolation(Rule rule, String fileName, Integer lineNumber, String message) {
        this.rule = rule;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.message = message;
    }
    
    public Rule getRule() {
        return rule;
    }
    
    public void setRule(Rule rule) {
        this.rule = rule;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "RuleViolation{" +
                "rule=" + (rule != null ? rule.getRuleId() : "null") +
                ", lineNumber=" + lineNumber +
                ", fileName='" + fileName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
