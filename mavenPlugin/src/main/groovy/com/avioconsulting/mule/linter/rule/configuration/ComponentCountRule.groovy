package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ComponentCountRule extends Rule {
    static final String RULE_ID = 'COMPONENT_COUNT'
    static final String RULE_NAME = 'A component should not be used more than the allowed number of times. '
    static final String RULE_VIOLATION_MESSAGE = ' was used more times than allowed'

    String ruleId
    String ruleName
    String component
    String namespace
    Integer count

    ComponentCountRule(String component, String namespace, Integer count) {
        this(RULE_ID, RULE_NAME, component, namespace, count)
    }

    ComponentCountRule(String ruleId, String ruleName, String component, String namespace, Integer count) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.component = component
        this.namespace = namespace
        this.count = count
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        if (application.findComponents(component, namespace).size() >= count) {
            violations.add(new RuleViolation(this,
                    application.findComponents(component, namespace).last().file.path,
                    application.findComponents(component, namespace).last().lineNumber,
                    component + RULE_VIOLATION_MESSAGE))
        }
        return violations
    }
}
