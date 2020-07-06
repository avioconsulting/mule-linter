package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.LoggerComponent
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class LoggerAttributesRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_NAMING'
    static final String RULE_NAME = 'Property File Naming Rule'
    static final String RULE_VIOLATION_MESSAGE = 'Missing property file, files must match naming pattern: '

    LoggerAttributesRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application application) {
        println('LoggerAttributesRule Executing on ' + application.name)

        List<ConfigurationFile> configs = application.configurationFiles
        application.configurationFiles.each {
            it.loggerComponents.each { log ->
                    println(log.message)
            }
        }
        return []
    }

    private void checkLogger(LoggerComponent log){
      
    }
}
