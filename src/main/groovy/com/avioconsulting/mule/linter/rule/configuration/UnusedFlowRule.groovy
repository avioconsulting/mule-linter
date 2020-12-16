package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class UnusedFlowRule extends Rule {

    static final String RULE_ID = 'UNUSED_FLOW'
    static final String RULE_NAME = 'All flows and sub-flows are used in application. '
    static final String RULE_VIOLATION_MESSAGE = 'The following flow is not referenced by any flow ref components: '

    UnusedFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<String> flowRefs = app.flowrefs*.name

        app.allFlows.each { flow ->
            if ( !flow.isApiKitFlow() && !flow.hasSource() ) {
                if ( !flowRefs.contains(flow.name) ) {
                    violations.add(new RuleViolation(this, flow.file.path,
                            flow.getLineNumber(), RULE_VIOLATION_MESSAGE + flow.name))
                }
            }
        }
        return violations
    }

}
