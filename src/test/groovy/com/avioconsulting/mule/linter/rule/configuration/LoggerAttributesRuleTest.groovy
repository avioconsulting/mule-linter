package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class LoggerAttributesRuleTest extends Specification {

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Logger Attributes check'() {
        given:
        Rule rule = new LoggerAttributesRule()

        when:
        File appDir = new File(this.class.classLoader.getResource('LoggingMuleApp').file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].fileName == 'simple-logging-flow-with-errors.xml'
        violations[0].lineNumber == 18
        violations[1].lineNumber == 35
    }
}
