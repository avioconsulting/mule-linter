package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class LoggerAttributesRule extends Rule {

    List<String> requiredAttributes
    static final String RULE_ID = 'LOGGER_ATTRIBUTES_RULE'
    static final String RULE_NAME = 'Logger Attributes Required Rule'
    static final String RULE_VIOLATION_MESSAGE = 'Logger is missing attribute '

    LoggerAttributesRule(List<String> requiredAttributes) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.requiredAttributes = requiredAttributes
    }

    LoggerAttributesRule(String ruleId, String ruleName, List<String> requiredAttributes) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.requiredAttributes = requiredAttributes
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.each { config ->
            config.findLoggerComponents().each { log ->
                requiredAttributes.each { att ->
                    if (!log.hasAttributeValue(att)) {
                        violations.add(new RuleViolation(this, config.name, log.lineNumber, RULE_VIOLATION_MESSAGE + att))
                    }
                }
            }
        }
        return violations
    }

}
