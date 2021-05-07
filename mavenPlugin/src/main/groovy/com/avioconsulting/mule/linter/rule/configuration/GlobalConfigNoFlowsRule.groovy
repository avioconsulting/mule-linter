package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class GlobalConfigNoFlowsRule extends Rule {

    static final String RULE_ID = 'GLOBAL_CONFIG_NO_FLOWS'
    static final String RULE_NAME = 'Global mule configuration xml exists and contain no flow and sub-flow. '
    static final String RULE_VIOLATION_MESSAGE = 'Mule configuration xml contain flow or sub-flow: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Mule global configuration xml does not exist.'
    static final String DEFAULT_FILE_NAME = 'globals.xml'
    String globalFileName

    GlobalConfigNoFlowsRule(String globalFileName) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.globalFileName = globalFileName
    }

    GlobalConfigNoFlowsRule() {
        this(DEFAULT_FILE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        Boolean globalFound = false

        app.configurationFiles.each {
            configFile ->
                if (configFile.name == globalFileName) {
                    globalFound = true
                    List<MuleComponent> flowsSubflowsConfigs =  configFile.findNonGlobalConfigs()
                    if (flowsSubflowsConfigs.size() > 0) {
                        flowsSubflowsConfigs.each {
                            violations.add(new RuleViolation(this, configFile.path,
                                    it.lineNumber, RULE_VIOLATION_MESSAGE + it.getAttributeValue('name')))
                        }
                    }
                }
        }

        if (!globalFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + app.CONFIGURATION_PATH,
                    0, FILE_MISSING_VIOLATION_MESSAGE ))
        }

        return violations
    }
}
