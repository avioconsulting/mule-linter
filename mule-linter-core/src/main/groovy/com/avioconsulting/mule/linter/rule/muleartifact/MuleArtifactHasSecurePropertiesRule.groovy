package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import org.apache.groovy.json.internal.JsonArray


class MuleArtifactHasSecurePropertiesRule extends Rule {

    static final String RULE_ID = 'MULE_ARTIFACT_SECURE_PROPERTIES'
    static final String RULE_NAME = 'The mule-artifact.json should contain certain secured properties. '
    static final String RULE_VIOLATION_MESSAGE = 'The secureProperties array does not contain the property '
    static final List<String> DEFAULT_PROPERTIES = ['anypoint.platform.client_secret']

    private List secureProperties
    private List properties
    private Boolean includeDefaults

    MuleArtifactHasSecurePropertiesRule() {
        this([], true)
    }

    MuleArtifactHasSecurePropertiesRule(List<String> properties, Boolean includeDefaults) {
        super(RULE_ID, RULE_NAME)
        this.includeDefaults = includeDefaults
        setProperties(properties)
    }

    List getProperties() {
        return properties
    }

    void setProperties(List properties) {
        this.properties = properties
        secureProperties = includeDefaults ? properties + DEFAULT_PROPERTIES : properties
    }

    Boolean getIncludeDefaults() {
        return includeDefaults
    }

    void setIncludeDefaults(Boolean includeDefaults) {
        this.includeDefaults = includeDefaults
        setProperties(this.properties)
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
