package com.avioconsulting.mule.linter.model;

class Rule {
    String ruleId
    String ruleName
    RuleSeverity severity = RuleSeverity.MINOR
    Application application

    Rule(String ruleId, String ruleName, Application app) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.application = app
        println("Created rule $ruleId $ruleName")
    }

    Application getApplication() {
        return application
    }

    void setApplication(Application application) {
        this.application = application
    }

    String getRuleName() {
        return ruleName
    }

    void setRuleName(String ruleName) {
        this.ruleName = ruleName
    }

    void setSeverity(RuleSeverity severity) {
        this.severity = severity
    }

/* Rule logic to be overridden */
    public List<RuleViolation> execute() {
        println("Override this method.")
        return null
    }




    @Override
    public String toString() {
        return "Rule{" +
                "ruleId='" + ruleId + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", severity=" + severity +
                '}';
    }
}
