package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.*

/**
 * This rule locates flow/sub-flow elements from inside a mule configuration xml files and validates
 * that number of components in the flow/sub-flow is less than the maxCount property defined in the linter rule.
 */
class FlowSubflowComponentCountRule extends Rule {
    static final String RULE_ID = 'FLOW_SUBFLOW_COMPONENT_COUNT'
    static final String RULE_NAME = 'Components in the Flow and subflow should not exceed allowed component count.'
    static final String RULE_VIOLATION_MESSAGE = ' has more than the defined components count. Components in the flow/sub-flow: '
    static final Integer DEFAULT_MAX_COUNT = 20

    /**
     * maxCount: maximum allowed components in a flow or sub-flow.
     * The default maxCount is `set to 20.
     */
    @Param("maxCount") Integer maxCount

    FlowSubflowComponentCountRule() {
        super(RULE_ID, RULE_NAME, RuleSeverity.MINOR, RuleType.CODE_SMELL)
        this.maxCount = DEFAULT_MAX_COUNT
    }

    FlowSubflowComponentCountRule(String ruleId, String ruleName, Integer maxCount) {
        super()
        this.maxCount = maxCount
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.allFlows.each {flow ->
            if(flow.children.size() > maxCount){
                violations.add(new RuleViolation(this, flow.file.path,
                        flow.lineNumber,
                        flow.name + RULE_VIOLATION_MESSAGE + flow.children.size()))
            }
        }
        return violations
    }
}
