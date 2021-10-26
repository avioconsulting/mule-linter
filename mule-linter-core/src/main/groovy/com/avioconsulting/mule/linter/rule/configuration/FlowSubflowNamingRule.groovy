package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class FlowSubflowNamingRule extends Rule {

    static final String RULE_ID = 'FLOW_SUBFLOW_NAMING'
    static final String RULE_NAME = 'Flow and subflow name is following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'Flow or subflow is not following naming conventions: '
    static final Map<String, String> flowSubFlowComponent = ['sub-flow': Namespace.CORE,
                                                            'flow': Namespace.CORE]
    CaseNaming caseNaming = new CaseNaming()

    FlowSubflowNamingRule(CaseNaming.CaseFormat format) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        caseNaming.setFormat(format)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.allFlows.each { flow ->
            if ( !flow.isApiKitFlow() && !caseNaming.isValidFormat(flow.name) ) {
                violations.add(new RuleViolation(this, flow.file.path,
                        flow.lineNumber, RULE_VIOLATION_MESSAGE + flow.name))
            }
        }
        return violations
    }
}
