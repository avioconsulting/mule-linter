package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
class PomPropertyValueRuleTest extends  Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()

        app = new Application(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Correct Property Value'() {
        when:
        Rule rule = new PomPropertyValueRule('munit.version', '2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Property Value'() {
        when:
        Rule rule = new PomPropertyValueRule('munit.version', '3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    def 'Missing Property'() {
        when:
        Rule rule = new PomPropertyValueRule('invalid.munit.version', '2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 12
    }

}
