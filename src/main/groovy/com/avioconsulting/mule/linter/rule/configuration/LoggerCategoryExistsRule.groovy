package com.avioconsulting.mule.linter.rule.configuration

class LoggerCategoryExistsRule extends LoggerAttributesRule {
    static final String RULE_ID = 'LOGGER_CATEGORY_HASVALUE'
    static final String RULE_NAME = 'Logger category attribute has a value'
    static final String ATTRIBUTE_NAME = 'category'

    LoggerCategoryExistsRule(){
        super(RULE_ID, RULE_NAME, [ATTRIBUTE_NAME])
    }
}
