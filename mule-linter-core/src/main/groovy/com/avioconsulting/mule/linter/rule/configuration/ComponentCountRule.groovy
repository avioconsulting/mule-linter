package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that a given component has not been used more than a specified number of times.
 * It exists as a customizable tool for a company to enforce a limit on the usage of a given component.
 */
class ComponentCountRule extends Rule {
    static final String RULE_ID = 'COMPONENT_COUNT'
    static final String RULE_NAME = 'A component should not be used more than the allowed number of times. '
    static final String RULE_VIOLATION_MESSAGE = ' was used more times than allowed'
    /** component: is the name of the mule component this rule should search for.
     * Examples include `"flow"` or `"request"`.
     */
    @Param("component") String component

    /**
     * namespace: is the namespace of the given mule component.
     * Examples include `"http://www.mulesoft.org/schema/mule/core"` or `"http://www.mulesoft.org/schema/mule/http"`.
     * The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`.
     */
    @Param("namespace") String namespace

    /**
     * maxCount: usage limit for the component
     */
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
