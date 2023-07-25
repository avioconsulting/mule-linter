package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import groovy.text.SimpleTemplateEngine

class AutoDiscoveryRule extends Rule {
    static final String RULE_ID = 'AUTO_DISCOVERY_EXISTS'
    static final String RULE_NAME = 'Mule API Autodiscovery configuration is present and configured in Global Configuration File'
    static final String MISSING_AUTODISCOVERY_MESSAGE = 'API Autodiscovery configuration is missing for the HTTP Listener Flow : '
    static final String API_ID_MISSING_VIOLATION_MESSAGE = 'apiId property is missing in the API Auto discovery configuration.'
    static final String DUPLICATE_API_ID_MESSAGE = ' property is found to be duplicate in the property files.'
    static final String MISSING_FILE_MESSAGE = 'Property cannot be found, Property File is missing: '
    static final String PROPERTY_MISSING_VIOLATION_MESSAGE = ' property is missing in the property file: '
    static final String API_ID_HARDCODED_VIOLATION_MESSAGE = 'API Autodiscovery configuration is not externalized into property files.'

    private static String AUTO_DISCOVERY_NAMESPACE = "http://www.mulesoft.org/schema/mule/api-gateway"
    private static String AUTO_DISCOVERY_COMPONENT = "autodiscovery"

    private static String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http"
    private static String HTTP_LISTENER_COMPONENT = "listener"

    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'
    static final List<String> DEFAULT_ENVIRONMENTS = ['dev', 'test', 'prod']

    /**
     * DEFAULT_API_IDS: is a default list of values for API IDs in the property files that will be replaced at deployment time.
     * API Auto Discovery rule will not check property value rules when apiId value matches in the DEFAULT_API_IDs list.
     * The default list is: ["-1", "0", "1"]
     */
    private static List DEFAULT_API_IDS = ["-1", "0", "1"]

    /**
     * environments: is a list of environments that the property must be found in.
     * This value is used when determining the name for property files to be searched.
     * The default list is: ['dev', 'test', 'prod']
     */
    @Param("environments") List<String> environments

    /**
     * pattern: is a custom naming scheme for property files loaded by environment.
     * The default pattern is `"${appname}-${env}.properties"`.
     */
    @Param("pattern") String pattern

    AutoDiscoveryRule() {
        super(RULE_ID, RULE_NAME, RuleSeverity.BLOCKER, RuleType.VULNERABILITY)
        this.environments = DEFAULT_ENVIRONMENTS
        this.pattern = DEFAULT_PATTERN
    }
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        List<String> httpFlowsList = getHttpListenerFlows(app)
        List<MuleComponent> autoDiscoveryComponents = app.findComponents(AUTO_DISCOVERY_COMPONENT, AUTO_DISCOVERY_NAMESPACE)
        httpFlowsList.each { httpFlow ->
            MuleComponent autoDiscoveryComponent = findMatchingAutoDiscoveryComponent(httpFlow, autoDiscoveryComponents)
            if (autoDiscoveryComponent != null) {
                // Check if apiId property exists for API Autodiscovery component
                if (!autoDiscoveryComponent.hasAttributeValue("apiId") || autoDiscoveryComponent.getAttributeValue("apiId").empty) {
                    violations.add(new RuleViolation(this, autoDiscoveryComponent.file.path,
                            autoDiscoveryComponent.lineNumber, API_ID_MISSING_VIOLATION_MESSAGE))
                } else {
                    String apiIdAttributeValue = autoDiscoveryComponent.getAttributeValue("apiId")
                    // Check if apiId property is externalized for API Autodiscovery, and not hardcoded
                    if (apiIdAttributeValue.startsWith('${')) {
                        String apiIdProperty = apiIdAttributeValue.substring(2, apiIdAttributeValue.length() - 1)
                        violations.addAll checkPropertyValueViolations(app, apiIdProperty)
                    } else {
                        violations.add(new RuleViolation(this, autoDiscoveryComponent.file.path,
                                autoDiscoveryComponent.lineNumber, API_ID_HARDCODED_VIOLATION_MESSAGE+" The Hard coded value for API ID is : "+apiIdAttributeValue))
                    }
                }
            } else {
                violations.add(new RuleViolation(this, httpFlow,0, MISSING_AUTODISCOVERY_MESSAGE + httpFlow))
            }
        }
        return violations
    }
    /**
     * This method returns the matching API Autodiscovery component for the flow with HTTP Listener component
     */
    MuleComponent findMatchingAutoDiscoveryComponent(String httpFlow,List<MuleComponent> muleComponentsList){
        MuleComponent autoDiscoveryComponent = null
        muleComponentsList.each {muleComp ->
            if(httpFlow == muleComp.getAttributeValue("flowRef")){
                autoDiscoveryComponent = muleComp
            }
        }
        return autoDiscoveryComponent
    }
    /**
     * This method checks the apiId property in each environment property file to make sure it exists
     * and is set to a dummy value (-1,0,1) to be overwritten at deployment time, or that each environment has a unique value.
     */
    List<RuleViolation> checkPropertyValueViolations(Application application,String propertyName) {
        List<RuleViolation> propertyViolations = []
        List apiIdsList = []
        environments.each { env ->
            Map<String, String> binding = ['appname':application.name, 'env':env]
            String fileName = new SimpleTemplateEngine().createTemplate(pattern).make(binding)
            PropertyFile pf = application.propertyFiles.find { it.getName() == fileName }
            if (pf == null) {
                propertyViolations.add(new RuleViolation(this, fileName, 0, MISSING_FILE_MESSAGE + fileName))
            } else if (pf.getProperty(propertyName) == null) {
                propertyViolations.add(new RuleViolation(this, pf.getFile().path, 0,
                        propertyName+ PROPERTY_MISSING_VIOLATION_MESSAGE + fileName))
            }else{
                String apiId = pf.getProperty(propertyName)
                if(DEFAULT_API_IDS.contains(apiId)){
                    // skip when apiId matches default list of values ["-1","0","1"], these values will be overridden at deployment
                }
                else if(apiIdsList.contains(apiId)) {
                    apiIdsList.add(apiId)
                    propertyViolations.add(new RuleViolation(this, pf.getFile().path, 0, propertyName + DUPLICATE_API_ID_MESSAGE))
                }else{
                    apiIdsList.add(apiId)
                }
            }
        }
        return propertyViolations
    }
    /**
     * This method returns the list of flow names for the flows with HTTP Listener component in the Mule application.
     */
    List<String> getHttpListenerFlows(Application application){
        List <String> httpFlows = []
        application.flows.each{ flow ->
            List<MuleComponent> muleCompList = flow.children
            if(muleCompList != null && muleCompList.size() > 0){
                muleCompList.each {muleComp ->
                    if (muleComp.componentName == HTTP_LISTENER_COMPONENT) {
                        httpFlows.add(flow.getAttributeValue("name"))
                    }
                }
            }
        }
        return httpFlows
    }
}