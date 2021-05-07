package com.avioconsulting.mule.linter.rule.configuration

class LoggerMessageExistsRule extends LoggerAttributesRule {

    static final String RULE_ID = 'LOGGER_MESSAGE_HASVALUE'
    static final String RULE_NAME = 'The Logger message attribute has a value. '
    static final String ATTRIBUTE_NAME = 'message'

/**
 * A logger attribute rule to enforce that a 'message' exists and is not empty.
 */
    LoggerMessageExistsRule() {
        super(RULE_ID, RULE_NAME, [ATTRIBUTE_NAME])
    }

}
