package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class GlobalConfigRule extends Rule {

    static final String RULE_ID = 'GLOBAL_CONFIG'
    static final String RULE_NAME = 'Global mule configuration xml exists and contain required configuration.'
    static final String RULE_VIOLATION_MESSAGE = 'Global mule configuration xml is missing required configuration: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Global mule configuration xml does not exist'
    //static final List<String> DEFAULT_CONFIGURATION_NAMES = ['secure-properties:config','http:listener-config']
    static final List<String> DEFAULT_CONFIGURATION_NAMES = ['config','listener-config']
    static final String DEFAULT_FILE_NAME = 'global-config.xml'
    static List<String> configurationNames

    GlobalConfigRule(List<String> configurationName) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.configurationNames = configurationName
    }

    GlobalConfigRule() {
        this(DEFAULT_CONFIGURATION_NAMES)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        ConfigurationFile globalConfigFile = app.getConfigurationFile(DEFAULT_FILE_NAME)

        if (globalConfigFile.doesExist()) {
            configurationNames.each { config ->
                if (!globalConfigFile.containsConfiguration(config)) {
                    violations.add(new RuleViolation(this, globalConfigFile.path,
                            1, RULE_VIOLATION_MESSAGE + config))
                }
            }
        } else {
            violations.add(new RuleViolation(this, globalConfigFile.path,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }

        return violations
    }
}
