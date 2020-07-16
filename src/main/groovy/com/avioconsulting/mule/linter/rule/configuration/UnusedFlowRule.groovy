package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class UnusedFlowRule extends Rule {

    static final String RULE_ID = 'UNUSED_FLOW'
    static final String RULE_NAME = 'All Flow and sub-flow are used in application.'
    static final String RULE_VIOLATION_MESSAGE = 'Flow or sub-flow is not referenced by flow ref: '

    UnusedFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<String> flowRefs = app.flowrefs*.name

        app.allFlows.each { flow ->
            if ( !flow.isApiKitFlow() && !flow.hasListner() ) {
                if ( !flowRefs.contains(flow.name) ) {
                    violations.add(new RuleViolation(this, flow.file.path,
                            flow.getLineNumber(), RULE_VIOLATION_MESSAGE + flow.name))
                }
            }
        }
        return violations
    }

}
