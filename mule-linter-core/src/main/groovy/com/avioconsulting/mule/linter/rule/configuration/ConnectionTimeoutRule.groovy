package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.Namespace

/**
 * This rule checks that connection timeout is configured for all the connectors that supports timeout in the mule application.
 * For all the connectors provided in the components list, it checks if connection timeout property 'responseTimeout' exits at the connector,
 * or configured at the connector configuration in the mule application.
 */
class ConnectionTimeoutRule extends Rule{
    static final String RULE_ID = 'CONNECTION_TIMEOUT_CONFIG'
    static final String RULE_NAME = 'Connectors must have an explicit timeout set, even if it’s identical to the default timeout value.'
    static final String RULE_VIOLATION_MESSAGE = 'Connector timeout is not configured for component: '

    static final String DEFAULT_TIMEOUT_ATTRIBUTE = 'responseTimeout'
    static final String DEFAULT_CONFIG_REF = 'request-config'

    /**
     * components: is a List of Maps containing `name`, `namespace`, `timeoutAttribute` and `config-ref`.
     * `timeoutAttribute` property is optional in the components list, and its defaulted to `responseTimeout`
     * `config-ref` property is optional in the components list, and its defaulted to `request-config`
     * The most common namespaces can be referenced from the class `com.avioconsulting.mule.linter.model.Namespace`.
     * This argument is optional. The default list is as follows:
     * [
     *  [name: 'request', namespace: Namespace.HTTP, timeoutAttribute: 'responseTimeout', 'config-ref': 'request-config']
     * ]
     */
    @Param("components") List components = [
            [name: 'request', namespace: Namespace.HTTP, timeoutAttribute: 'responseTimeout', 'config-ref': 'request-config']
    ]

    ConnectionTimeoutRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.MAJOR
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        components.each { Map<String, String> component ->
            String timeoutAttribute = component.timeoutAttribute ?: DEFAULT_TIMEOUT_ATTRIBUTE
            String configRef = component.'config-ref' ?: DEFAULT_CONFIG_REF
            application.findComponents(component.name, component.namespace).each {muleComponent ->
                // check if connection timeout attribute is configured at component,
                // if connection timeout attribute is not configured at component, check for timeoutAttribute in the connector configuration.
                if(!muleComponent.attributes.containsKey(timeoutAttribute)){
                    boolean isTimeoutConfigured = false
                    application.findComponents(configRef, component.namespace).each{configRefMuleComponent ->
                        if(configRefMuleComponent.getAttributeValue("name") == muleComponent.getAttributeValue("config-ref")){
                            if(configRefMuleComponent.attributes.containsKey(timeoutAttribute)
                                    && configRefMuleComponent.getAttributeValue(timeoutAttribute) != ''){
                                isTimeoutConfigured = true
                            }
                        }
                    }
                    if (!isTimeoutConfigured) {
                        violations.add(new RuleViolation(this, muleComponent.file.path, muleComponent.lineNumber,
                                RULE_VIOLATION_MESSAGE + muleComponent.componentName))
                    }
                }
                else if(muleComponent.attributes.containsKey(timeoutAttribute)
                        && muleComponent.getAttributeValue(timeoutAttribute) == ''){
                    violations.add(new RuleViolation(this, muleComponent.file.path, muleComponent.lineNumber,
                            RULE_VIOLATION_MESSAGE + muleComponent.componentName))
                }
            }
        }
        return violations
    }
}
