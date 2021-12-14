package com.avioconsulting.mule.linter.rule.configuration

/**
 * This rule checks that the category attribute is present on all loggers.
 * The category attribute should be present to make Mule logs easier to search.
 */
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
