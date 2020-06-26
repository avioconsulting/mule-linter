package com.avioconsulting.mule.linter.model;

class Rule {
    String ruleId = "TBD"
    String ruleName
    String severity = "ERROR"
    Application application

    Rule(String ruleId, String ruleName, Application app) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.application = app
    }

    Application getApplication() {
        return application
    }

    void setApplication(Application application) {
        this.application = application
    }

    Application application() {
        return application
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
