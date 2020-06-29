package com.avioconsulting.mule.linter.model;

class Rule {
    String ruleId
    String ruleName
    RuleSeverity severity = RuleSeverity.MINOR

    Rule(String ruleId, String ruleName) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        println("Created rule $ruleId $ruleName")
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
    public List<RuleViolation> execute(Application application) {
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
