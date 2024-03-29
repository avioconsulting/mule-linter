package com.avioconsulting.mule.linter.rule.pom


import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
class MuleMavenPluginVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Mule Maven Plugin Version Check'() {
        given:
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MuleMavenPluginVersionRule()
        rule.version = version
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '3.3.5' | 0
        '3.3.6' | 1
    }

}
