package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.groovy.json.internal.JsonArray

/**
 * This rule checks that certain properties are secured within the `mule-artifact.json` file found at root.
 * Some properties should be listed in the securedProperties Array in `mule-artifact.json`, to prevent keys from being shown in plain text in Anypoint Runtime Manager.
 */
class MuleArtifactHasSecurePropertiesRule extends Rule {

    static final String RULE_ID = 'MULE_ARTIFACT_SECURE_PROPERTIES'
    static final String RULE_NAME = 'The mule-artifact.json should contain certain secured properties. '
    static final String RULE_VIOLATION_MESSAGE = 'The secureProperties array does not contain the property '
    static final List<String> DEFAULT_PROPERTIES = ['anypoint.platform.client_secret']

    List<String> secureProperties

    /**
     * properties: is a List of values that should be in the securedProperties Array of the `mule-artifact.json`.
     * By default, this list is:
     * ['anypoint.platform.client_id', 'anypoint.platform.client_secret']
     */
    @Param("properties") List<String> properties

    /**
     * includeDefaults: should be `true` if the developer wishes to include AVIO's default values, and `false` otherwise.
     * The defaults to include are `'anypoint.platform.client_id'` and `'anypoint.platform.client_secret'`.
     */
    @Param("includeDefaults") Boolean includeDefaults

    MuleArtifactHasSecurePropertiesRule() {
        super(RULE_ID, RULE_NAME)
        this.properties = []
        this.includeDefaults = true
    }

    @Override
    void init(){
        secureProperties = includeDefaults ? properties + DEFAULT_PROPERTIES : properties
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        JsonArray sprops = app.muleArtifact.secureProperties
        secureProperties.each { prop ->
            if (!sprops?.contains(prop)) {
                violations.add(new RuleViolation(this, app.muleArtifact.name, sprops == null ? 0 : sprops.lineNumber,
                        RULE_VIOLATION_MESSAGE + prop))
            }
        }
        return violations
    }

}
