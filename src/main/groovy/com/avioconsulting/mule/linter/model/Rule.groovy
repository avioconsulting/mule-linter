package com.avioconsulting.mule.linter.model;

abstract class Rule {

    String ruleId
    String ruleName
    RuleSeverity severity = RuleSeverity.MINOR

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
    abstract List<RuleViolation> execute(Application application)

    @Override
    String toString() {
        return 'Rule{' +
                'ruleId=\'' + ruleId + '\'' +
                ', ruleName=\'' + ruleName + '\'' +
                ', severity=\'' + severity +
                '}';
    }
}
