package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule ensures that developers are taking full advantage of display name when considering certain components in Anypoint Studio.
 * "Set Variable" does not tell a developer what a component does, and requires a developer to switch to XML view or click on each component to know anything about what a component is doing.
 * The default list covers the most egregious examples of vague naming. You may provide your own list if you require more (or less) components to have names.
 */
class DisplayNameRule extends Rule {

    static final String RULE_ID = 'COMPONENT_DISPLAY_NAME'
    static final String RULE_NAME = 'Display name is not default. '
    static final String RULE_VIOLATION_MESSAGE = 'Display name should not be default for component: '

    /**
     * components: is a List of Maps containing `name`, `namespace`, and `displayName`.
     * The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`.
     * This argument is optional. The default list is as follows:
     * [
     *  [name: 'set-payload', namespace: 'http://www.mulesoft.org/schema/mule/core', displayName: 'Set Payload'],
     *  [name: 'set-variable', namespace: 'http://www.mulesoft.org/schema/mule/core', displayName: 'Set Variable'],
     *  [name: 'transform', namespace: 'http://www.mulesoft.org/schema/mule/ee/core', displayName: 'Transform Message'],
     *  [name: 'flow-ref', namespace: 'http://www.mulesoft.org/schema/mule/core', displayName: 'Flow Reference']
     * ]
     */
    @Param("components") List components = [
                [name: 'set-payload', namespace: Namespace.CORE, displayName: 'Set Payload'],
                [name: 'set-variable', namespace: Namespace.CORE, displayName: 'Set Variable'],
                [name: 'transform', namespace: Namespace.CORE_EE, displayName: 'Transform Message'],
                [name: 'flow-ref', namespace: Namespace.CORE, displayName: 'Flow Reference']
    ]

    DisplayNameRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        components.each { Map<String, String> component ->
            application.findComponents(component.name, component.namespace).each {
                if (!it.attributes.containsKey('{'+ Namespace.DOC + '}name') ||
                    it.attributes.get('{'+ Namespace.DOC + '}name') == component.displayName) {
                    violations.add(new RuleViolation(this, it.file.path, it.lineNumber,
                            RULE_VIOLATION_MESSAGE + component.name))
                }
            }
        }
        return violations
    }
}
