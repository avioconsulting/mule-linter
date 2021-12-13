package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

/**
 * This rule checks that the contents of a logger do not match a given Regular Expression.
 * AVIO recommends that a developer never deploy code that logs the full payload at the level of INFO.
 * The Mule message payload can contain sensitive information, and logging it at the default log level might persist sensitive information for longer than intended or make it visible to people who shouldn't see it.
 */
class LoggerMessageContentsRule extends Rule {

    static final String RULE_ID = 'LOGGER_MESSAGE_CONTENTS'
    static final String RULE_NAME = 'The Logger message does not contain undesired info. '
    static final String RULE_VIOLATION_MESSAGE = 'Logger is not allowed to have this message '
    static final String LOGGER_LEVEL = "INFO"
    static final Pattern DEFAULT_REGEX = ~/payload]/

    Pattern rulePattern
    Map<String, Pattern> rulesMap

    /**
     * pattern: is an optional rule to specify the regex that you would like to search for in the log message.
     * By default, the regex is `~/payload]/`, which will catch basic payload logging, but allow for selecting individual fields.
     */
    @Param("pattern") String pattern

    /**
     * rules: is a map where the key is the level of the logger, and the value is the regex that you would like to search for in the log message.
     * The default stance of the rule is to only check INFO, which is equivalent to:
     *  ['INFO':'~/payload]/']
     */
    @Param("rules") Map<String, String> rules

    LoggerMessageContentsRule() {
        super(RULE_ID, RULE_NAME)
        this.rulePattern = DEFAULT_REGEX
    }

    @Override
    void init(){
        if(pattern != null) {
            this.rulePattern = Pattern.compile(pattern)
        }
        if(rules != null) {
            Map<String, Pattern> rulesParam = new HashMap<>()

            rules.forEach((key, value)->{
                rulesParam.put(key as String, Pattern.compile(value as String))
            })
            this.rulesMap = rulesParam
        }
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.collect({it.findLoggerComponents()}).flatten().each {
            LoggerComponent loggerComponent ->
                if (rulesMap != null) {
                    if (rulesMap.any {loggerComponent.level == it.key && loggerComponent.message =~ it.value}) {
                        violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                                RULE_VIOLATION_MESSAGE + loggerComponent.message))
                    }
                } else if (loggerComponent.level == LOGGER_LEVEL && loggerComponent.message =~ rulePattern) {
                    violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                            RULE_VIOLATION_MESSAGE + loggerComponent.message))
                }}
        return violations
    }
}
