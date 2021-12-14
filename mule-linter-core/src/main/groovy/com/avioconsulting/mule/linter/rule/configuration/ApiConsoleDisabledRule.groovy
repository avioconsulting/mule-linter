package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that API Console has been removed or disabled (flow has attribute `initialState="stopped"`).
 * API console has been the source of security vulnerabilities in the past, and is unnecessary outside of a local development environment thanks to Anypoint Exchange.
 * AVIO recommends that deployments outside of local testing should not enable it.
 */
class ApiConsoleDisabledRule extends Rule {

    static final String RULE_ID = 'API_CONSOLE_DISABLED'
    static final String RULE_NAME = 'API Console Disabled Rule. '
    static final String RULE_VIOLATION_MESSAGE = 'API Console should be removed or disabled before deployment'

    ApiConsoleDisabledRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.flows.findAll({it.children.any({it.componentName == 'console'})}).each {
            if (!it.hasAttributeValue('initialState') || it.getAttributeValue('initialState') != 'stopped') {
                violations.add(new RuleViolation(this, it.file.path, it.lineNumber, RULE_VIOLATION_MESSAGE))
            }
        }
        return violations
    }
}
