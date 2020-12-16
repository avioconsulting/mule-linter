package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import org.apache.groovy.json.internal.JsonString

class MuleArtifactMinMuleVersionRule extends Rule {

    static final String RULE_ID = 'MULE_ARTIFACT_MIN_MULE_VERSION'
    static final String RULE_NAME = 'The minMuleVersion attribute in mule-artifact.json should be less than ' +
            'the app.runtime value in the pom.xml. '
    static final String MISSING_PROPERTY_MESSAGE = 'The minMuleVersion property is missing from mule-artifact.json'
    static final String PROPERTY_MISMATCH_MESSAGE = 'The minMuleVersion property in mule-artifact.json is greater ' +
            'than the app.runtime version in the pom.xml. '

    MuleArtifactMinMuleVersionRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        JsonString minMuleVersion = application.muleArtifact.minMuleVersion
        String appRuntime = application.pomFile.getPomProperty('app.runtime').value

        if (minMuleVersion == null) {
            violations.add(new RuleViolation(this, application.muleArtifact.path, 0, MISSING_PROPERTY_MESSAGE))
        } else if (appRuntime < minMuleVersion.toString()) {
            violations.add(new RuleViolation(this, application.pomFile.path, minMuleVersion.lineNumber,
                    PROPERTY_MISMATCH_MESSAGE + 'app.runtime: ' + appRuntime + ' minMuleVersion: ' + minMuleVersion))
        }
        return violations
    }

}
