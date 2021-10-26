package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application

abstract class Rule {

    String ruleId
    String ruleName
    RuleSeverity severity = RuleSeverity.MINOR
    RuleType ruleType = RuleType.CODE_SMELL

    String getRuleName() {
        return ruleName
    }

    void setRuleName(String ruleName) {
        this.ruleName = ruleName
    }

    void setSeverity(RuleSeverity severity) {
        this.severity = severity
    }

    void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType
    }

    abstract List<RuleViolation> execute(Application application)

}
