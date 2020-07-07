package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.MuleComponent
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class GlobalConfigRule extends Rule {

    static final String RULE_ID = 'GLOBAL_CONFIG'
    static final String RULE_NAME = 'Global mule configuration xml exists and contain required configuration.'
    static final String RULE_VIOLATION_MESSAGE = 'Mule configuration xml contain global configuration: '
    static Map<String, String> DEFAULT_NON_GLOBAL = ['sub-flow': 'http://www.mulesoft.org/schema/mule/core',
                                                      'flow'    : 'http://www.mulesoft.org/schema/mule/core']
    static Map<String, String> noneGlobalElements
    static String DEFAULT_FILE_NAME = 'globals.xml'
    String globalFileName

    GlobalConfigRule(String globalFileName, Map<String, String> noneGlobalElements) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.noneGlobalElements = noneGlobalElements
        this.globalFileName = globalFileName
    }

    GlobalConfigRule() {
        this(DEFAULT_FILE_NAME, DEFAULT_NON_GLOBAL)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.configurationFiles.each {
            configFile ->
            List<MuleComponent> globalConfigs = configFile.findChildComponents(noneGlobalElements,false)
            if (globalConfigs.size() > 0 && configFile.name != globalFileName) {
                globalConfigs.each {
                    violations.add(new RuleViolation(this, configFile.path,
                            it.lineNumber, RULE_VIOLATION_MESSAGE + it.componentName))
                }
            }
        }

        return violations
    }
}
