package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class MunitVersionRuleTest extends Specification {

    private static final String APP_NAME = 'SampleMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Correct MUnit Version'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new MunitVersionRule('2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect MUnit Version'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing MUnit Property'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    private Application loadApp() {
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        new Application(appDir)
    }

}
