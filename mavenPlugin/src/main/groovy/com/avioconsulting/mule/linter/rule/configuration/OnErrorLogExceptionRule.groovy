package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class OnErrorLogExceptionRule extends Rule {

    static final String RULE_ID = 'ON_ERROR_LOG_EXCEPTION'
    static final String RULE_NAME = 'Exception should be logged after an error. '
    static final String RULE_VIOLATION_MESSAGE = 'Log exception enabled is required for ' +
                                                    'on-error-continue and on-error-propagate'
    private static final List<String> ON_ERROR_COMPONENTS = ['on-error-continue', 'on-error-propagate']
    private static final String ATTRIBUTE_NAME = 'logException'
    private static final String ATTRIBUTE_VALUE_CHECK = 'true'

    OnErrorLogExceptionRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.each { file ->
            ON_ERROR_COMPONENTS.each {
                file.findComponents(it, Namespace.CORE).each { comp ->
                    if (comp.getAttributeValue(ATTRIBUTE_NAME) != ATTRIBUTE_VALUE_CHECK) {
                        violations.add(new RuleViolation(this, file.name, comp.lineNumber, RULE_VIOLATION_MESSAGE))
                    }
                }
            }
        }
        return violations
    }

}
