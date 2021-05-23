package com.avioconsulting.mule.linter.rule.autodiscovery

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class AutoDiscoveryRule extends Rule {
    static final String RULE_ID = 'AUTO_DISCOVERY_EXISTS'
    static final String RULE_NAME = 'Mule API Autodiscovery configuration is present and configured in Global Configuration File'
    static final String MISSING_AUTODISCOVERY_MESSAGE = 'Mule API Autodiscovery configuration is missing in Global configuration File '
    static final String DEFAULT_GLOBAL_CONFIG_FILE_NAME = 'global-config.xml'
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'Mule Global configuration XML does not exist'
    static final String AUTODISCOVERY_NOT_EXTERNALIZED_MESSAGE = 'Mule API Autodiscovery configuration is not externalized in the File  :  '
    String globalFileName

    AutoDiscoveryRule(String globalFileName) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.BLOCKER
        this.globalFileName = globalFileName
    }

    AutoDiscoveryRule() {
        this(DEFAULT_GLOBAL_CONFIG_FILE_NAME)
    }
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        Boolean isGlobalConfigFileFound = false
        Boolean isAutoDiscoveryFound = false
        Boolean isHttpFound = false
        if(app.configurationFiles!=null && app.configurationFiles.size() >0) {
            app.configurationFiles.each {
                configFile ->
                    if (configFile!=null && configFile.name.equals(globalFileName) ) {
                        isGlobalConfigFileFound = true
                        List<MuleComponent> muleComp = configFile.findGlobalConfigs()
                        if(muleComp!=null && muleComp.size()>0) {
                            muleComp.each {
                                mc ->
                                    if((mc.componentNamespace.contains("/schema/mule/http" ) && mc.componentName.equals("listener-config")) || mc.componentNamespace.contains("/schema/mule/mule-apikit")) {
                                        isHttpFound = true
                                    }
                                    if (mc.componentName == "autodiscovery") {
                                        isAutoDiscoveryFound = true
                                        if(!mc.isExternalized(mc.getAttributeValue('apiId'))){
                                            violations.add(new RuleViolation(this, configFile.file.path, mc.lineNumber, AUTODISCOVERY_NOT_EXTERNALIZED_MESSAGE + configFile.file.name +". The Hard coded value for API ID is : "+mc.getAttributeValue('apiId')))
                                        }
                                    }
                            }
                        }
                    }
            }
        }
        // If Autodiscovery is not present for a Mule Application with HTTP Listener end point or APIKIT router
        if (isGlobalConfigFileFound && isHttpFound && !isAutoDiscoveryFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + "/"+ app.CONFIGURATION_PATH+"/"+DEFAULT_GLOBAL_CONFIG_FILE_NAME,
                    0, MISSING_AUTODISCOVERY_MESSAGE))
        }
        if (!isGlobalConfigFileFound) {
            violations.add(new RuleViolation(this, app.applicationPath.absolutePath + "/" +app.CONFIGURATION_PATH+"/"+DEFAULT_GLOBAL_CONFIG_FILE_NAME,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }

        return violations
    }
}