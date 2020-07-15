package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.MuleComponent
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class UnusedFlowRule extends Rule {

    static final String RULE_ID = 'UNUSED_FLOW'
    static final String RULE_NAME = 'All Flow and sub-flow are used in application.'
    static final String RULE_VIOLATION_MESSAGE = 'Flow or sub-flow is not referenced by flow ref: '
    private static final String ATTRIBUTE_NAME = 'name'
    static final String APIKIT_FLOW_PREFIX_REGEX = '(get:|post:|put:|patch:|delete:|head:|options:|trace:).*'

    UnusedFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<String> flowRefs = app.findComponents('flow-ref',
                ConfigurationFile.MULE_CORE_NAMESPACE)*.getAttributeValue(ATTRIBUTE_NAME)

        app.allFlows.each { flow ->
            String flowName = flow.getAttributeValue(ATTRIBUTE_NAME)
            MuleComponent firstComp = flow.children[0]
            if ( !flowName.toLowerCase().matches(APIKIT_FLOW_PREFIX_REGEX) &&
                    !firstComp.componentName.toLowerCase().contains('listener') ) {
                if ( !flowRefs.contains(flowName) ) {
                    violations.add(new RuleViolation(this, flow.file.path,
                            flow.getLineNumber(), RULE_VIOLATION_MESSAGE + flowName))
                }
            }
        }
        return violations
    }
}
