package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that certain common config elements use String interpolation placeholcers (`${value}`) to point to provided properties rather than be specified statically in line.
 * Referring to properties with placeholders makes it easier to specify values by environment, encrypt secrets that should not be plain text values, or provide common values through templates or archetypes.
 * This rule only considers global configuration elements, and does not look at values provided within flows.
 * The default list covers the most common attributes to provide placeholders for, but is not exhaustive. You may provide your own list if necessary.
 */
class ConfigPlaceholderRule extends Rule{

    static final String RULE_ID = 'CONFIG_PLACEHOLDER'
    static final String RULE_NAME = 'Common Configs should have placeholders for certain elements. '
    static final String RULE_VIOLATION_MESSAGE = 'Config should have a placeholder for attribute: '

    /**
     * placeholderAttributes are the component attributes that the rule should require to be placeholders.
     * This argument is optional. The default array is as follows:
     * ['key', 'password', 'keyPassword', 'username', 'host', 'clientId', 'clientSecret',
     * 'tokenUrl', 'domain', 'workstation', 'authDn', 'authPassword', 'authentication',
     * 'url', 'localCallbackUrl', 'externalCallbackUrl',
     * 'localAuthorizationUrlResourceOwnerId', 'localAuthorizationUrl',
     * 'authorizationUrl', 'passphrase']
     */
    @Param("placeholderAttributes") def placeholderAttributes = ['key', 'password', 'keyPassword', 'username', 'host', 'clientId', 'clientSecret',
                                      'tokenUrl', 'domain', 'workstation', 'authDn', 'authPassword', 'authentication',
                                      'url', 'localCallbackUrl', 'externalCallbackUrl',
                                      'localAuthorizationUrlResourceOwnerId', 'localAuthorizationUrl',
                                      'authorizationUrl', 'passphrase']

    ConfigPlaceholderRule() {
        super(RULE_ID, RULE_NAME)
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
