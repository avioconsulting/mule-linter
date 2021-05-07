package com.avioconsulting.mule.linter.rule.configuration

class LoggerCategoryExistsRule extends LoggerAttributesRule {

    static final String RULE_ID = 'LOGGER_CATEGORY_HASVALUE'
    static final String RULE_NAME = 'The Logger category attribute has a value. '
    static final String ATTRIBUTE_NAME = 'category'

/**
 * A logger attribute rule to enforce that a 'category' exists and is not empty.
 */
    LoggerCategoryExistsRule() {
        super(RULE_ID, RULE_NAME, [ATTRIBUTE_NAME])
    }

}
