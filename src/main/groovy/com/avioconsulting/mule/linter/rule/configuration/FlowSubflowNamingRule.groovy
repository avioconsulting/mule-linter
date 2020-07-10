package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.MuleComponent
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class FlowSubflowNamingRule extends Rule {

    static final String RULE_ID = 'FLOW_SUBFLOW_NAMING'
    static final String RULE_NAME = 'Flow and subflow name is following naming conventions.'
    static final String RULE_VIOLATION_MESSAGE = 'Flow or subflow is not following naming conventions: '
    static final Map<String, String> flowSubFlowComponent = ['sub-flow':ConfigurationFile.MULE_CORE_NAMESPACE,
                                                            'flow':ConfigurationFile.MULE_CORE_NAMESPACE]
    CaseNaming caseNaming = new CaseNaming()

    FlowSubflowNamingRule(CaseNaming.CaseFormat format) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        caseNaming.setFormat(format)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.configurationFiles.each {
            configFile ->
            flowSubFlowComponent.each {
                element->
                List<MuleComponent> flowsSubflows =  configFile.findComponents(element.key,element.value)
                flowsSubflows.each {
                    flowSubflow ->
                    String flowSubflowName = flowSubflow.getAttributeValue('name')
                    if (!caseNaming.isValidFormat(flowSubflowName)) {
                        violations.add(new RuleViolation(this, configFile.path,
                                flowSubflow.getLineNumber(), RULE_VIOLATION_MESSAGE + flowSubflowName))
                    }
                }
            }
        }
        return violations
    }
}
