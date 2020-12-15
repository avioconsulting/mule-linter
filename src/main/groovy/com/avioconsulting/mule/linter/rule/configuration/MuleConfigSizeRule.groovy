package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MuleConfigSizeRule extends Rule {

    static final String RULE_ID = 'MULE_CONFIG_SIZE'
    static final String RULE_NAME = 'Mule Config files should not be too long. '
    static final String RULE_VIOLATION_MESSAGE = 'Mule Config has too many flows in it.'
    Integer flowLimit = 20

    MuleConfigSizeRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    MuleConfigSizeRule(Integer flowLimit) {
        this()
        this.flowLimit = flowLimit
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.each {
            if (it.flows.size() + it.subFlows.size() > flowLimit) {
                violations.add(new RuleViolation(this, it.file.path, 0, RULE_VIOLATION_MESSAGE))
            }
        }
        return violations
    }
}
