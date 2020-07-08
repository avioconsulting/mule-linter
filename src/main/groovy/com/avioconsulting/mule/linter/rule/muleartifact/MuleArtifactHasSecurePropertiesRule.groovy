package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import org.apache.groovy.json.internal.JsonArray

class MuleArtifactHasSecurePropertiesRule extends Rule {

    static final String RULE_ID = 'MULE_ARTIFACT_SECURE_PROPERTIES'
    static final String RULE_NAME = 'mule-artifact.json should contain certain secured properties'
    static final List<String> DEFAULT_PROPERTIES = ['anypoint.platform.client_id', 'anypoint.platform.client_secret']

    private final List secureProperties

    MuleArtifactHasSecurePropertiesRule() {
        this([], true)
    }

    MuleArtifactHasSecurePropertiesRule(List<String> properties, Boolean includeDefaults) {
        ruleId = RULE_ID
        ruleName = RULE_NAME
        secureProperties = includeDefaults ? properties + DEFAULT_PROPERTIES : properties
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        JsonArray sprops = app.muleArtifact.secureProperties
        secureProperties.each { prop ->
            if (!sprops.contains(prop)) {
                violations.add(new RuleViolation(this, app.muleArtifact.name, sprops.lineNumber,
                        'The secureProperties array does not contain the property ' + prop))
            }
        }
        return violations
    }

}
