package com.avioconsulting.mule.linter.rule.autodiscovery

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class AutoDiscoveryRule extends Rule {
    static final String RULE_ID = 'AUTO_DISCOVERY_CONFIG_PRESENT'
    static final String RULE_NAME = 'Auto discovery configuration is present and configured in Global Configuration File  '
    static final String MISSING_AUTODISCOVERY_MESSAGE = 'The Auto discovery configuration is missing from Global configuration File '
    static final String DEFAULT_GLOBAL_CONFIG_FILE_NAME = 'global-config.xml'
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Mule global configuration xml does not exist'
    static final String AUTODISCOVERY_NOT_EXTERNALIZED_MESSAGE = 'The Auto discovery configuration is Configured but API ID is Hard coded in the file :  '

    String globalFileName


    AutoDiscoveryRule(String globalFileName) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.CRITICAL
        this.globalFileName = globalFileName
    }

    AutoDiscoveryRule() {

        this(DEFAULT_GLOBAL_CONFIG_FILE_NAME)

    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        //Auto discovery configuration would be checked only in Global Config file
        Boolean globalConfigFileFound = false
        Boolean autoDiscoveryFound = false
        // Iterate over each configuration file using Application Model
        app.configurationFiles.each {
                // Assign the file to a temporary variable configFile
            configFile ->
                //Check if the FIle is a Global File
                if (configFile.name == globalFileName) {
                    globalConfigFileFound = true
                    // Get all configurations with in the Global file (global-config.xml)
                    List<MuleComponent> globalConfigs = configFile.findGlobalConfigs()
                    // Iterate over each global config element
                    globalConfigs.each {
                        globalConfigEachElement ->
                            if (globalConfigEachElement.componentName == "autodiscovery") {

                                autoDiscoveryFound = true

                                if(globalConfigEachElement.getAttributeValue('apiId')!=null && !(globalConfigEachElement.getAttributeValue('apiId').startsWith('Mule::p(')
                                        || globalConfigEachElement.getAttributeValue('apiId').startsWith('p(')
                                        || globalConfigEachElement.getAttributeValue('apiId').startsWith('${')
                                        || globalConfigEachElement.getAttributeValue('apiId').startsWith('#[') )) {
                                    violations.add(new RuleViolation(this, configFile.file.path, globalConfigEachElement.lineNumber, AUTODISCOVERY_NOT_EXTERNALIZED_MESSAGE+configFile.file.name))
                                }
                            }
                    }

                }
        }

        // If Global config file exits but Autodiscovery is present in Global config throw the below error

        if (globalConfigFileFound && !autoDiscoveryFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + "/"+ app.CONFIGURATION_PATH+"/"+DEFAULT_GLOBAL_CONFIG_FILE_NAME,
                    0, MISSING_AUTODISCOVERY_MESSAGE))
        }

// Thrown an error if global config does not exist
        if (!globalConfigFileFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + "/" +app.CONFIGURATION_PATH+"/"+DEFAULT_GLOBAL_CONFIG_FILE_NAME,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }

        return violations
    }
}