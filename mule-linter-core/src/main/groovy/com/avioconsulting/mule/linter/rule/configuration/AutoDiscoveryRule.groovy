package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
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
     * enabled: is a boolean flag, when set to 'false' API Autodiscovery rule will be skipped.
     * The default value is: true
     */
    @Param("enabled") boolean enabled

    /**
     * exemptedFlows: is a list of flows with HTTP Listener components that will be exempted for API Autodiscovery.
     * The default list is: []
     */
    @Param("exemptedFlows") List<String> exemptedFlows

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
        this.enabled = true
        this.exemptedFlows= []
    }
    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        // Execute API Autodiscovery rule, only when enabled is set to 'true'
        if (this.enabled == true){
            List<FlowComponent> httpFlowsList = getHttpListenerFlows(application)
            List<MuleComponent> autoDiscoveryComponents = application.findComponents(AUTO_DISCOVERY_COMPONENT, AUTO_DISCOVERY_NAMESPACE)
            httpFlowsList.each { httpFlow ->
                String httpFlowName = httpFlow.getAttributeValue("name")
                MuleComponent autoDiscoveryComponent = findMatchingAutoDiscoveryComponent(httpFlowName, autoDiscoveryComponents)
                if (autoDiscoveryComponent != null) {
                    violations.addAll validateAutodiscoveryComponent(application, autoDiscoveryComponent)
                } else {
                    violations.add(new RuleViolation(this, httpFlow.file.path,0, MISSING_AUTODISCOVERY_MESSAGE + httpFlowName))
                }
            }
        }
        return violations
    }
    /**
     * This method validates Autodiscovery component and returns list of RuleViolation.
     * returns List<RuleViolation>
     */
    List<RuleViolation> validateAutodiscoveryComponent(Application application, MuleComponent autoDiscoveryComponent)
    {
        List<RuleViolation> autodiscoveryViolations = []
        // Check if apiId property exists for API Autodiscovery component
        if (!autoDiscoveryComponent.hasAttributeValue("apiId") || autoDiscoveryComponent.getAttributeValue("apiId").empty) {
            autodiscoveryViolations.add(new RuleViolation(this, autoDiscoveryComponent.file.path,
                    autoDiscoveryComponent.lineNumber, API_ID_MISSING_VIOLATION_MESSAGE))
        } else {
            autodiscoveryViolations.addAll validateAutodiscoveryApiId(application, autoDiscoveryComponent)
        }
        return autodiscoveryViolations
    }

    /**
     * This method validates apiId property in Autodiscovery component and returns list of RuleViolation.
     * returns List<RuleViolation>
     */
    List<RuleViolation> validateAutodiscoveryApiId(Application application, MuleComponent autoDiscoveryComponent)
    {
        List<RuleViolation> apiIdViolations = []
        String apiIdAttributeValue = autoDiscoveryComponent.getAttributeValue("apiId")
        // Check if apiId property is externalized for API Autodiscovery, and not hardcoded
        if (apiIdAttributeValue.startsWith('${')) {
            String apiIdProperty = apiIdAttributeValue.substring(2, apiIdAttributeValue.length() - 1)
            apiIdViolations.addAll checkPropertyValueViolations(application, apiIdProperty)
        } else {
            apiIdViolations.add(new RuleViolation(this, autoDiscoveryComponent.file.path,
                    autoDiscoveryComponent.lineNumber, API_ID_HARDCODED_VIOLATION_MESSAGE+" The Hard coded value for API ID is : "+apiIdAttributeValue))
        }
        return apiIdViolations
    }

    /**
     * This method returns the matching API Autodiscovery component for the flow with HTTP Listener component
     */
    MuleComponent findMatchingAutoDiscoveryComponent(String flowName,List<MuleComponent> muleComponentsList){
        MuleComponent autoDiscoveryComponent = null
        muleComponentsList.each {muleComp ->
            if(flowName == muleComp.getAttributeValue("flowRef")){
                autoDiscoveryComponent = muleComp
            }
        }
        return autoDiscoveryComponent
    }
    /**
     * This method checks the apiId property in each environment property file to make sure it exists
     * and is set to a dummy value (-1,0,1) to be overwritten at deployment time, or that each environment has a unique value.
     * Ret
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
                if(!(DEFAULT_API_IDS.contains(apiId)) && apiIdsList.contains(apiId)) {
                    apiIdsList.add(apiId)
                    propertyViolations.add(new RuleViolation(this, pf.getFile().path, 0, propertyName + DUPLICATE_API_ID_MESSAGE))
                }else if(!DEFAULT_API_IDS.contains(apiId) ){
                    apiIdsList.add(apiId)
                }
            }
        }
        return propertyViolations
    }

    /**
     * This method returns the list of flows with HTTP Listener component in the Mule application, and exempts the flows that are available in exemptedFlows.
     */
    List<FlowComponent> getHttpListenerFlows(Application application){
        List <FlowComponent> httpFlows = []
        application.flows.each{ flow ->
            String flowName = flow.getAttributeValue("name")
            if(!exemptedFlows.contains(flowName)){
                List<MuleComponent> muleCompList = flow.children
                if(muleCompList != null && muleCompList.size() > 0){
                    muleCompList.each {muleComp ->
                        if (muleComp.componentName == HTTP_LISTENER_COMPONENT) {
                            httpFlows.add(flow)
                        }
                    }
                }
            }
        }
        return httpFlows
    }
}