package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule ensures that `flow` and `sub-flow` names follow a given case format.
 * Files, properties, and certain name attributes should follow a self consistent case pattern for readability and predictability.
 * Here, we require the user to specify a particular case convention.
 */
class FlowSubflowNamingRule extends Rule {

    static final String RULE_ID = 'FLOW_SUBFLOW_NAMING'
    static final String RULE_NAME = 'Flow and subflow name is following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'Flow or subflow is not following naming conventions: '
    static final Map<String, String> flowSubFlowComponent = ['sub-flow': Namespace.CORE,
                                                             'flow': Namespace.CORE]
    CaseNaming caseNaming

    /**
     * naming format for this rule. the default value is `KEBAB_CASE`
     * Current options are `CAMEL_CASE`, `PASCAL_CASE`, `SNAKE_CASE`, or `KEBAB_CASE`.
     */
    @Param("format") String format

    FlowSubflowNamingRule(){
        caseNaming = new CaseNaming(CaseNaming.CaseFormat.KEBAB_CASE)
    }

    @Override
    void init(){
        if(format != null)
            try {
                caseNaming.setFormat((CaseNaming.CaseFormat.valueOf(format)))
            }catch(Exception e){
                throw new IllegalArgumentException("Invalid format value: '"+format+"'. Current options are: " + CaseNaming.CaseFormat.values() )
            }

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
