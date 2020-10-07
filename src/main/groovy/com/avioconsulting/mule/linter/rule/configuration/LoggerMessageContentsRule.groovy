package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class LoggerMessageContentsRule extends Rule {

    static final String RULE_ID = 'LOGGER_MESSAGE_CONTENTS_RULE'
    static final String RULE_NAME = 'Logger Message Contents Rule'
    static final String RULE_VIOLATION_MESSAGE = 'Logger of level INFO or higher is logging Payload'
    static final String LOGGER_LEVEL = "INFO"
    static final String LOGGER_REGEX = ~/payload]/

    LoggerMessageContentsRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.collect({it.findLoggerComponents()}).flatten().each {
            LoggerComponent loggerComponent ->
                if (loggerComponent.level == LOGGER_LEVEL && loggerComponent.message =~ LOGGER_REGEX) {
                    violations.add(new RuleViolation(this, loggerComponent.file.path,
                            loggerComponent.lineNumber, RULE_VIOLATION_MESSAGE))
                }}
        return violations
    }
}
