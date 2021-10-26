package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class DisplayNameRule extends Rule {

    static final String RULE_ID = 'DISPLAY_NAME'
    static final String RULE_NAME = 'Display name is not default. '
    static final String RULE_VIOLATION_MESSAGE = 'Display name should not be default for component: '

    List components = [[name: 'set-payload', namespace: Namespace.CORE, displayName: 'Set Payload'],
                       [name: 'set-variable', namespace: Namespace.CORE, displayName: 'Set Variable'],
                       [name: 'transform', namespace: Namespace.CORE_EE, displayName: 'Transform Message'],
                       [name: 'flow-ref', namespace: Namespace.CORE, displayName: 'Flow Reference']]

    DisplayNameRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    DisplayNameRule(List components) {
        this()
        this.components = components
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
