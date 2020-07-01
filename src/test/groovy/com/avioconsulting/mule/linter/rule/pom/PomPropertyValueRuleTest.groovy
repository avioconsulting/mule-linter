package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class PomPropertyValueRuleTest extends  Specification {

    private static final String APP_NAME = 'SampleMuleApp'

    def 'Correct Property Value'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new PomPropertyValueRule('munit.version', '2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Property Value'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new PomPropertyValueRule('munit.version', '3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    def 'Missing Property'() {
        given:
        Application app = loadApp()

        when:
        Rule rule = new PomPropertyValueRule('invalid.munit.version', '2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 12
    }

    private Application loadApp() {
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        new Application(appDir)
    }
}