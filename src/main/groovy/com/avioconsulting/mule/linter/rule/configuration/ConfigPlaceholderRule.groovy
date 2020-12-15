package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ConfigPlaceholderRule extends Rule{

    static final String RULE_ID = 'CONFIG_PLACEHOLDER'
    static final String RULE_NAME = 'Common Configs should have placeholders for certain elements. '
    static final String RULE_VIOLATION_MESSAGE = 'Config should have a placeholder for attribute: '

    String[] placeholderAttributes = ['key', 'password', 'keyPassword', 'username', 'host', 'clientId', 'clientSecret',
                                      'tokenUrl', 'domain', 'workstation', 'authDn', 'authPassword', 'authentication',
                                      'url', 'localCallbackUrl', 'externalCallbackUrl',
                                      'localAuthorizationUrlResourceOwnerId', 'localAuthorizationUrl',
                                      'authorizationUrl', 'passphrase']

    ConfigPlaceholderRule() {
        ruleId = RULE_ID
        ruleName = RULE_NAME
    }

    ConfigPlaceholderRule(String[] placeholderAttributes) {
        this()
        this.placeholderAttributes = placeholderAttributes
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.globalConfigs.each {
            violations.addAll checkForViolation(it)
        }
        return violations
    }

    List<RuleViolation> checkForViolation(MuleComponent component) {
        List<RuleViolation> violations = []
        component.attributes.each {
            if(placeholderAttributes.contains(it.key) && !(it.value.startsWith('${') && it.value.endsWith('}'))) {
                violations.add(new RuleViolation(this, component.file.path, component.lineNumber,
                        RULE_VIOLATION_MESSAGE + it.key))
            }
        }
        component.children.each {violations.addAll checkForViolation(it)}
        return violations
    }
}
