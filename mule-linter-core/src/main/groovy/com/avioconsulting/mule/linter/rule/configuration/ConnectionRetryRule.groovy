package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that connection retry strategy is configured for all the connectors that supports retry in the mule application.
 * For all the connectors provided in the components list, check if reconnect configuration with property `frequency` and `count` exists at the connector,
 * or configured at the connector configuration in the mule application.
 */
class ConnectionRetryRule extends Rule{
    static final String RULE_ID = 'CONNECTION_RETRY_CONFIG'
    static final String RULE_NAME = 'Connectors must have an reconnection strategy configured, where its supported.'
    static final String RULE_VIOLATION_MESSAGE = 'Connection Retry is not configured for component: '

    private static final String RETRY_ELEMENT = 'reconnect'
    private static final String RETRY_FREQUENCY = 'frequency'
    private static final String RETRY_COUNT = 'count'
    private static final String DEFAULT_CONFIG_REF = 'request-config'

    /**
     * components: is a List of Maps containing `name`, `namespace`, and `config-ref`.
     * `config-ref` property is optional in the components list, and its defaulted to `request-config`
     * The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`.
     * This argument is optional. The default list is as follows:
     * [
     * [name: 'request', namespace: Namespace.HTTP, 'config-ref': 'request-config']
     * ]
     */
    @Param("components") List components = [
            [name: 'request', namespace: Namespace.HTTP, 'config-ref': 'request-config']
    ]

    ConnectionRetryRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.MAJOR
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        components.each { Map<String, String> component ->
            String configRef = component.'config-ref' ?: DEFAULT_CONFIG_REF
            application.findComponents(component.name, component.namespace).each {muleComponent ->
                // check if retry strategy is configured at component,
                // if retry is not configured at component, check for retry in the configuration of the connector.
                if(!checkReconnectStrategy(muleComponent)) {
                    boolean isRetryConfigured = false
                    application.findComponents(configRef, component.namespace).each {configRefMuleComponent ->
                        if (configRefMuleComponent.getAttributeValue("name") == muleComponent.getAttributeValue("config-ref")) {
                            if(checkReconnectStrategy(configRefMuleComponent)) {
                                isRetryConfigured = true
                            }
                        }
                    }
                    if (!isRetryConfigured) {
                        violations.add(new RuleViolation(this, muleComponent.file.path, muleComponent.lineNumber,
                                RULE_VIOLATION_MESSAGE + muleComponent.componentName))
                    }
                }
            }
        }
        return violations
    }
    /**
     * This method iterates through MuleComponent and returns `true` if reconnect configuration exists for the component.
     * returns boolean
     */
    boolean checkReconnectStrategy(MuleComponent muleComponent){
        boolean isRetryConfigured = false
        if(muleComponent.componentName == RETRY_ELEMENT
                && muleComponent.hasAttributeValue(RETRY_FREQUENCY) && muleComponent.hasAttributeValue(RETRY_COUNT)){
            isRetryConfigured = true
        }else if(muleComponent.children.size() > 0){
            for (MuleComponent childMuleComponent : muleComponent.children){
                if(checkReconnectStrategy(childMuleComponent)){
                    isRetryConfigured = true
                    break
                }
            }
        }else if (muleComponent.children.size()==0){
            isRetryConfigured = false
        }
        return isRetryConfigured
    }
}
