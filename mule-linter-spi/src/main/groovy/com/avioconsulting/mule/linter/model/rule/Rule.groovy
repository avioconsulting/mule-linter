package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application

abstract class Rule {

    String ruleId
    String ruleName
    RuleSeverity severity = RuleSeverity.MINOR
    RuleType ruleType = RuleType.CODE_SMELL

    Rule(String ruleId, String ruleName, RuleSeverity severity, RuleType ruleType) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.severity = severity
        this.ruleType = ruleType
    }
    Rule(String ruleId, String ruleName) {
        this(ruleId, ruleName, RuleSeverity.CRITICAL, RuleType.CODE_SMELL)
    }

    static Rule createRule(Class ruleClass,Map<String, Object> params) {
        Object ruleObj
        if(params == null || params.isEmpty())
            return ruleClass.newInstance() as Rule

        ruleObj = ruleClass.createRule(params)
        return ruleObj as Rule
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

    void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType
    }

    abstract List<RuleViolation> execute(Application application)

}
