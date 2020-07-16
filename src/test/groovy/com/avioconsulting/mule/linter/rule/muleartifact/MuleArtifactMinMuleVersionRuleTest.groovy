package com.avioconsulting.mule.linter.rule.muleartifact

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleArtifact
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class MuleArtifactMinMuleVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Values match'() {
        given:
        testApp.addMuleArtifact()
        app = new Application(testApp.appDir)

        when:
        Rule rule = new MuleArtifactMinMuleVersionRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'app.runtime less than minMuleVersion ' () {
        given:
        testApp.addFile(MuleArtifact.MULE_ARTIFACT_JSON, APPVERSION_LESSTHAN_MINMULE)
        app = new Application(testApp.appDir)

        when:
        Rule rule = new MuleArtifactMinMuleVersionRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 2
        violations[0].fileName.endsWith('pom.xml')
        violations[0].rule.ruleId == MuleArtifactMinMuleVersionRule.RULE_ID
    }

    def 'app.runtime greater than minMuleVersion ' () {
        given:
        testApp.addFile(MuleArtifact.MULE_ARTIFACT_JSON, APPVERSION_GREATERTHAN_MINMULE)
        app = new Application(testApp.appDir)

        when:
        Rule rule = new MuleArtifactMinMuleVersionRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Missing minMuleVersion from MuleArtifact' () {
        given:
        testApp.addFile(MuleArtifact.MULE_ARTIFACT_JSON, MISSING_VERSION_MULE_ARTIFACT)
        app = new Application(testApp.appDir)

        when:
        Rule rule = new MuleArtifactMinMuleVersionRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].fileName.endsWith('mule-artifact.json')
        violations[0].rule.ruleId == MuleArtifactMinMuleVersionRule.RULE_ID
    }

    private static final String APPVERSION_LESSTHAN_MINMULE = '''{
  "minMuleVersion": "5.2.0",
  "secureProperties": [
    "anypoint.platform.client_id",
    "anypoint.platform.client_secret"
  ]
}
'''
    private static final String APPVERSION_GREATERTHAN_MINMULE = '''{
  "minMuleVersion": "4.1.3",
  "secureProperties": [
    "anypoint.platform.client_id",
    "anypoint.platform.client_secret"
  ]
}
'''
    private static final String MISSING_VERSION_MULE_ARTIFACT = '''{
  "secureProperties": [
    "anypoint.platform.client_id",
    "anypoint.platform.client_secret"
  ]
}'''

}
