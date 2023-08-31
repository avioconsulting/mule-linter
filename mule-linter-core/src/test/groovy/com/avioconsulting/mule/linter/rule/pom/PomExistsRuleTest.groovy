package com.avioconsulting.mule.linter.rule.pom


import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class PomExistsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Maven Pom Exists check'() {
        given:
        testApp.addPom()
        Rule rule = new PomExistsRule()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Maven Pom Does not exist check'() {
        given:
        Rule rule = new PomExistsRule()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == PomExistsRule.FILE_NOT_EXISTS
    }
}
