package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class GlobalConfigRule extends Rule {

    static final String RULE_ID = 'GLOBAL_CONFIG'
    static final String RULE_NAME = 'Global mule configuration xml exists and contain required configuration. '
    static final String RULE_VIOLATION_MESSAGE = 'Mule configuration xml contain global configuration: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Mule global configuration xml does not exist'
    static final String DEFAULT_FILE_NAME = 'globals.xml'
    static Map<String, String> noneGlobalElements = [:]
    String globalFileName

    GlobalConfigRule(String globalFileName, Map<String, String> noneGlobalElements) {
        this(globalFileName)
        this.noneGlobalElements += noneGlobalElements
    }

    GlobalConfigRule(String globalFileName) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.globalFileName = globalFileName
    }

    GlobalConfigRule() {
        this(DEFAULT_FILE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        Boolean globalFound = false
        app.configurationFiles.each {
            configFile ->
            if (noneGlobalElements.size() > 0) {
                configFile.addAdditionalGlobalConfig(noneGlobalElements)
            }
            List<MuleComponent> globalConfigs = configFile.findGlobalConfigs()
            if (globalConfigs.size() > 0 && configFile.name != globalFileName) {
                globalConfigs.each {
                    violations.add(new RuleViolation(this, configFile.path,
                            it.lineNumber, RULE_VIOLATION_MESSAGE + it.componentName))
                }
            }
            if (configFile.name == globalFileName) {
                globalFound = true
            }
        }

        if (!globalFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + app.CONFIGURATION_PATH,
                    0, FILE_MISSING_VIOLATION_MESSAGE ))
        }

        return violations
    }

}
