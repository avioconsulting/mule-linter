package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.LoggerComponent
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

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        println('LoggerAttributesRule Executing on ' + application.name)

        application.configurationFiles.each { config ->
            println("Checking $config.name for Loggers.")
            config.loggerComponents.each { log ->
//                println("Found " + log.getName())
                requiredAttributes.each { att ->
                    if( !log.hasAttribute(att) ) {
                        violations.add(new RuleViolation(this, config.name, log.lineNumber, RULE_VIOLATION_MESSAGE + att))
                    }
                }
            }
        }
        return violations
    }

}
