package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class MuleRuntimeVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.create()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Mule Runtime Version Check'() {
        given:
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MuleRuntimeVersionRule(version)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '4.2.1' | 0
        '4.2.2' | 1
    }

}
