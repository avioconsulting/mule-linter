package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class LoggerAttributesRule extends Rule {

    static final String RULE_ID = 'LOGGER_ATTRIBUTES_RULE'
    static final String RULE_NAME = 'Loggers have required attributes. '
    static final String RULE_VIOLATION_MESSAGE = 'Logger is missing attribute '
    List<String> requiredAttributes

/**
 * A logger attribute rule to enforce that an attribute exists and is not empty.
 * @param requiredAttributes A list of required attributes
 */
    LoggerAttributesRule(@Param("requiredAttributes") List<String> requiredAttributes) {
        this(RULE_ID, RULE_NAME, requiredAttributes)
    }

/**
 * A constructor to override the generic implementation for specific attributes.
 * @param ruleId Rule Id for the overriding implemention
 * @param ruleName Rule Name for the overriding implmentation
 * @param requiredAttributes List of required attributes
 */
    LoggerAttributesRule(String ruleId, String ruleName, List<String> requiredAttributes) {
        super(ruleId, ruleName)
        this.requiredAttributes = requiredAttributes
    }

    static LoggerAttributesRule createRule(Map<String, Object> params){
        List<String> requiredAttributes = params.get("requiredAttributes") as List<String>
        if(requiredAttributes != null)
            return new LoggerAttributesRule(requiredAttributes)
        else
            throw new NoSuchFieldException("requiredAttributes")
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
