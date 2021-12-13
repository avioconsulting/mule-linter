package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ComponentCountRule extends Rule {
    static final String RULE_ID = 'COMPONENT_COUNT'
    static final String RULE_NAME = 'A component should not be used more than the allowed number of times. '
    static final String RULE_VIOLATION_MESSAGE = ' was used more times than allowed'

    @Param("component") String component
    @Param("namespace") String namespace
    @Param("maxCount") Integer maxCount

    ComponentCountRule(){
        super(RULE_ID, RULE_NAME)
    }
    ComponentCountRule(String ruleId, String ruleName, String component, String namespace, Integer maxCount) {
        super(ruleId, ruleName)
        this.component = component
        this.namespace = namespace
        this.maxCount = maxCount
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []

        def components = application.findComponents(component, namespace)
        if (components.size() > maxCount) {
            violations.add(new RuleViolation(this,
                    components.last().file.path,
                    components.last().lineNumber,
                    component + RULE_VIOLATION_MESSAGE))
        }
        return violations
    }
}
