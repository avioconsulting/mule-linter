package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that a global mule config exists, and that the expected configuration elements are not in other files.
 * AVIO expects this file to be called `globals.xml` by default.
 */
class GlobalConfigExistsRule extends Rule {

    static final String RULE_ID = 'GLOBAL_CONFIG_EXISTS'
    static final String RULE_NAME = 'Global mule configuration xml exists and contain required configuration. '
    static final String RULE_VIOLATION_MESSAGE = 'Mule global configuration xml does not exist: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Mule global configuration xml does not exist'
    static final String DEFAULT_FILE_NAME = 'globals.xml'

    @Param("noneGlobalElements") Map<String, String> noneGlobalElements

    /**
     * globalFileName: is the name of the file expected to contain global configuration elements for the Mule Application.
     * This value is expected to be `globals.xml` by default.
     */
    @Param("globalFileName") String globalFileName

    GlobalConfigExistsRule() {
        super(RULE_ID, RULE_NAME)
        this.noneGlobalElements = [:]
        this.globalFileName = DEFAULT_FILE_NAME
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
            if (globalConfigs.size() > 0 && !(configFile.name.matches(globalFileName))) {
                globalConfigs.each {
                    violations.add(new RuleViolation(this, configFile.path,
                            it.lineNumber, RULE_VIOLATION_MESSAGE + it.componentName))
                }
            }
            if (configFile.name.matches(globalFileName) ) {
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
