package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.MuleArtifact
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class MuleArtifactHasSecurePropertiesRuleTest extends Specification {

    private MuleApplication app
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addMuleArtifact()
        app = new MuleApplication(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Default Secure Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.muleArtifact != null
        app.muleArtifact.secureProperties.size() > 0
        violations.size() == 0
    }

    def 'Different and Default Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()
        rule.setProperty("properties",['new.property'])
        rule.setProperty("includeDefaults",true)
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Different Properties Missing,  Default Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()
        rule.setProperty("properties",['new.property.2', 'new.property.3'])
        rule.setProperty("includeDefaults",true)
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.muleArtifact != null
        violations.size() == 2
        violations[0].fileName == 'mule-artifact.json'
        violations[0].message.contains('new.property.2')
        violations[1].message.contains('new.property.3')
        violations[0].lineNumber == 3
    }

    def 'Default Properties Missing'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()
        rule.init()

        when:
        testApp.addFile(MuleArtifact.MULE_ARTIFACT_JSON, MISSING_PROPS_MULE_ARTIFACT)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.muleArtifact != null
        violations.size() == 1
        violations[0].lineNumber == 3
        violations[0].message.contains('client_secret')
    }

    def 'Missing Secure Properties'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()
        rule.init()

        when:
        testApp.addFile(MuleArtifact.MULE_ARTIFACT_JSON, MISSING_SECURE_PROPS)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.muleArtifact != null
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message.contains('client_secret')
    }

    private static final String MISSING_PROPS_MULE_ARTIFACT = '''{
  "minMuleVersion": "4.2.2",
  "secureProperties": [
    "new.property"
  ]
}'''
    private static final String MISSING_SECURE_PROPS = '''{
  "minMuleVersion": "4.2.2"
}'''
}
