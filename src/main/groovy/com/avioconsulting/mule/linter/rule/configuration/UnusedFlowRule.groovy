package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.MuleComponent
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class UnusedFlowRule extends Rule {

    static final String RULE_ID = 'UNUSED_FLOW'
    static final String RULE_NAME = 'All Flow and sub-flow are used in application.'
    static final String RULE_VIOLATION_MESSAGE = 'Flow and sub-flow is not referenced by flow ref: '
    static final Map<String, String> flowSubFlowComponent = ['sub-flow': ConfigurationFile.MULE_CORE_NAMESPACE,
                                                             'flow':ConfigurationFile.MULE_CORE_NAMESPACE]
    static final String APIKIT_FLOW_PREFIX_REGEX = '(get:|post:|put:|patch:|delete:|head:|options:|trace:).*'
    UnusedFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    List<MuleComponent> getAllFlow(Application app) {
        List<MuleComponent> allFlowsSubflows = []
        app.configurationFiles.each { configFile ->
            flowSubFlowComponent.each { element ->
                List<MuleComponent> flowsSubflows = configFile.findComponents(element.key, element.value)
                allFlowsSubflows += flowsSubflows
            }
        }
        return allFlowsSubflows
    }

    List<MuleComponent> getAllFlowRef(Application app) {
        List<MuleComponent> allFlowRefs = []
        app.configurationFiles.each { configFile ->
            List<MuleComponent> flowRefs = configFile.findComponents('flow-ref',
                                                                            ConfigurationFile.MULE_CORE_NAMESPACE)
            allFlowRefs += flowRefs
        }
        return allFlowRefs
    }

    List<MuleComponent> notUsedByFlowRef(List<MuleComponent> allFlowsSubflows, List<MuleComponent> allFlowRefs) {
        List<MuleComponent> notUsedByFlowRef = []
        allFlowsSubflows.each { flow->
            String flowSubflowName = flow.getAttributeValue('name')
            List<MuleComponent> foundRef = allFlowRefs.findAll { it.getAttributeValue('name') == flowSubflowName }

            if ( foundRef.size <= 0 ) {
                notUsedByFlowRef += flow
            }
        }
        return notUsedByFlowRef
    }

    List<MuleComponent> filterWithoutSource(List<MuleComponent> muleComponent) {
        List<MuleComponent> withoutSourceComp = []
        muleComponent.each { comp->
            MuleComponent firstComp = comp.children[0]
            if ( !firstComp.componentName.toLowerCase().contains('listener') ) {
                withoutSourceComp += comp
            }
        }
        return withoutSourceComp
    }

    List<MuleComponent> filterApiKitRouterFlow(List<MuleComponent> muleComponent) {
        List<MuleComponent> withoutApiKitRouterFlow = []
        muleComponent.each { comp->
            if ( !comp.getAttributeValue('name').toLowerCase().matches(APIKIT_FLOW_PREFIX_REGEX)) {
                withoutApiKitRouterFlow += comp
            }
        }
        return withoutApiKitRouterFlow
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<MuleComponent> allFlowsSubflows = getAllFlow(app)

        List<MuleComponent> allFlowRefs = getAllFlowRef(app)

        List<MuleComponent> notUsedByFlowRef = notUsedByFlowRef(allFlowsSubflows, allFlowRefs)

        List<MuleComponent> withoutSourceComp = filterWithoutSource(notUsedByFlowRef)

        List<MuleComponent> withoutApiKitRouterFlow = filterApiKitRouterFlow(withoutSourceComp)

        //make a list of unused flowname/subflow
        //loop through config files again check for name attribute matches
        //through violation

        return violations
    }
}
