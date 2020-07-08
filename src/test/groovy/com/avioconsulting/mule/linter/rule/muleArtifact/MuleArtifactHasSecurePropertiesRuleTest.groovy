package com.avioconsulting.mule.linter.rule.muleArtifact

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class MuleArtifactHasSecurePropertiesRuleTest extends Specification {

    private static final String TEST_APP = 'SampleMuleApp'
    private static final String TEST_APP_2 = 'BadMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Default Secure Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(TEST_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Different and Default Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule(['new.property'], true)

        when:
        File appDir = new File(this.class.classLoader.getResource(TEST_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Different Properties Missing,  Default Properties Exist'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule(['new.property.2', 'new.property.3'], true)

        when:
        File appDir = new File(this.class.classLoader.getResource(TEST_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].fileName == 'mule-artifact.json'
        violations[0].message.contains('new.property.2')
        violations[1].message.contains('new.property.3')
        violations[0].lineNumber == 3
    }

    def 'Default Properties Missing'() {
        given:
        Rule rule = new MuleArtifactHasSecurePropertiesRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(TEST_APP_2).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].lineNumber == 3
        violations[0].message.contains('client_id')
        violations[1].message.contains('client_secret')
    }

}
