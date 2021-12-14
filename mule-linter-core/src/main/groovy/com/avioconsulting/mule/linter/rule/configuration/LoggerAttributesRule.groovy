package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that the Loggers have required attributes.
 */
class LoggerAttributesRule extends Rule {

    static final String RULE_ID = 'LOGGER_REQUIRED_ATTRIBUTES'
    static final String RULE_NAME = 'Loggers have required attributes. '
    static final String RULE_VIOLATION_MESSAGE = 'Logger attribute is missing: '

    /**
     * requiredAttributes: List of required attributes to check in this rule
     */
    @Param("requiredAttributes") List<String> requiredAttributes

    LoggerAttributesRule() {
        this(RULE_ID, RULE_NAME,[])
    }

    LoggerAttributesRule(String ruleId, String ruleName, List<String> requiredAttributes) {
        super(ruleId, ruleName)
        this.requiredAttributes = requiredAttributes
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.each { config ->
            config.findLoggerComponents().each { log ->
                requiredAttributes.each { att ->
                    if (!log.hasAttributeValue(att)) {
                        violations.add(new RuleViolation(this, config.path, log.lineNumber,
                                RULE_VIOLATION_MESSAGE + att))
                    }
                }
            }
        }
        return violations
    }

}
