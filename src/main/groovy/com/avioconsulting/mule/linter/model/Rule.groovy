package com.avioconsulting.mule.linter.model;

class Rule {
    String ruleId = "TBD"
    String ruleName
    String severity = "ERROR"
    ProjectFile projectFile

    Rule(String ruleId, String ruleName, ProjectFile file) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.projectFile = file
    }

    ProjectFile getProjectFile() {
        return projectFile
    }

    void setSeverity(String severity) {
        this.severity = severity
    }

/* Rule logic to be overridden */
    public void execute() {}

    public void raiseIssue(Integer lineNumber, String message){
        System.out.println(severity + ": Rule " + ruleName + " failed validation on line " + lineNumber + " " + message)
    }
}
