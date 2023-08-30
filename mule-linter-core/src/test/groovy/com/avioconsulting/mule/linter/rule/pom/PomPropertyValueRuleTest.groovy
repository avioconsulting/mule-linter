package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
class PomPropertyValueRuleTest extends  Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()

        app = new MuleApplication(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Correct Property Value'() {
        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'munit.version'
        rule.propertyValue = '2.2.1'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Property Value'() {
        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'munit.version'
        rule.propertyValue = '3.2.1'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message  == 'munit.version maven property value does not match expected value. Expected: 3.2.1 found: 2.2.1'
    }

    def 'Missing Property'() {
        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'invalid.munit.version'
        rule.propertyValue = '2.2.1'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'invalid.munit.version does not exist in <properties></properties>'
    }

}
