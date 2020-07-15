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
    static final Map<String, String> flowSubFlowComponent = ['sub-flow':ConfigurationFile.MULE_CORE_NAMESPACE,
                                                             'flow':ConfigurationFile.MULE_CORE_NAMESPACE]
    private static final String ATTRIBUTE_NAME = 'name'
    static final String APIKIT_FLOW_PREFIX_REGEX = '(get:|post:|put:|patch:|delete:|head:|options:|trace:).*'

    UnusedFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    static List<MuleComponent> getAllFlowRef(Application app) {
        List<MuleComponent> allFlowRefs = []
        app.configurationFiles.each { configFile ->
            List<MuleComponent> flowRefs = configFile.findComponents('flow-ref',
                                                                            ConfigurationFile.MULE_CORE_NAMESPACE)
            allFlowRefs += flowRefs
        }
        return allFlowRefs
    }

    static List<MuleComponent> filterApiKitWithoutSource(Application app) {
        List<MuleComponent> allFilteredFlowsSubflows = []
        List<MuleComponent> filteredFlowsSubflows = []
        app.configurationFiles.each { configFile ->
            flowSubFlowComponent.each { element ->
                List<MuleComponent> flowsSubflows = configFile.findComponents(element.key, element.value)
                allFilteredFlowsSubflows += flowsSubflows
            }
        }
        allFilteredFlowsSubflows.each { comp ->
            MuleComponent firstComp = comp.children[0]
            if ( !comp.getAttributeValue(ATTRIBUTE_NAME).toLowerCase().matches(APIKIT_FLOW_PREFIX_REGEX) &&
                    !firstComp.componentName.toLowerCase().contains('listener')) {
                filteredFlowsSubflows += comp
            }
        }
        return filteredFlowsSubflows
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<MuleComponent> withoutApiKitRouterFlows = filterApiKitWithoutSource(app)
        List<MuleComponent> allFlowRefs = getAllFlowRef(app)

        List<MuleComponent> notReferencedFlow = withoutApiKitRouterFlows.findAll { flow ->
            String flowSubflowName = flow.getAttributeValue(ATTRIBUTE_NAME)
            !allFlowRefs.any { it.getAttributeValue(ATTRIBUTE_NAME) == flowSubflowName }
        }

        List<String> unusedFlowName = notReferencedFlow*.getAttributeValue(ATTRIBUTE_NAME)

        app.configurationFiles.each { configFile ->
            flowSubFlowComponent.each { element ->
                List<MuleComponent> flowsSubflows = configFile.findComponents(element.key, element.value)
                flowsSubflows.each { flowsSubflow ->
                    String flowSubflowName = flowsSubflow.getAttributeValue(ATTRIBUTE_NAME)
                    if ( unusedFlowName.contains(flowSubflowName) ) {
                        violations.add(new RuleViolation(this, configFile.path,
                                flowsSubflow.getLineNumber(), RULE_VIOLATION_MESSAGE + flowSubflowName))
                    }
                }
            }
        }
        return violations
    }
}
