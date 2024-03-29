package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that the Mule Configuration file does not contain too many flows or subflows.
 */
class MuleConfigFlowLimitRule extends Rule {

    static final String RULE_ID = 'MULE_CONFIG_FLOW_LIMIT'
    static final String RULE_NAME = 'Mule Config files should not be too long. '
    static final String RULE_VIOLATION_MESSAGE = 'Mule Config has too many flows in it.'

    /**
     * flowLimit: is the number of flows and subflows required to fail the rule.
     * By default this number of flows to fail the rule is `20`.
     */
    @Param("flowLimit") Integer flowLimit

    MuleConfigFlowLimitRule() {
        super(RULE_ID, RULE_NAME)
        this.flowLimit = 20
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
