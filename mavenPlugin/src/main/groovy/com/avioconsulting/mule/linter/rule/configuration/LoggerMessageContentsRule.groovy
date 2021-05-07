package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

class LoggerMessageContentsRule extends Rule {

    static final String RULE_ID = 'LOGGER_MESSAGE_CONTENTS'
    static final String RULE_NAME = 'The Logger message does not contain undesired info. '
    static final String RULE_VIOLATION_MESSAGE = 'Logger is not allowed to have this message '
    static final String LOGGER_LEVEL = "INFO"
    static final Pattern DEFAULT_REGEX = ~/payload]/

    Pattern pattern
    Map<String, Pattern> rules

    LoggerMessageContentsRule() {
        this(DEFAULT_REGEX)
    }

    LoggerMessageContentsRule(Pattern pattern) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.pattern = pattern
    }

    LoggerMessageContentsRule(Map<String, Pattern> rules) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.rules = rules
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.collect({it.findLoggerComponents()}).flatten().each {
            LoggerComponent loggerComponent ->
                if (rules != null) {
                    if (rules.any {loggerComponent.level == it.key && loggerComponent.message =~ it.value}) {
                        violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                                RULE_VIOLATION_MESSAGE + loggerComponent.message))
                    }
                } else if (loggerComponent.level == LOGGER_LEVEL && loggerComponent.message =~ pattern) {
                    violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                            RULE_VIOLATION_MESSAGE + loggerComponent.message))
                }}
        return violations
    }
}
