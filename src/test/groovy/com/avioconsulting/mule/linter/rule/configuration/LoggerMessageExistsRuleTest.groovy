package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class LoggerMessageExistsRuleTest extends Specification {

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Logger Attributes check'() {
        given:
        Rule rule = new LoggerMessageExistsRule()

        when:
        File appDir = new File(this.class.classLoader.getResource('LoggingMuleApp').file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName == 'simple-logging-flow-with-errors.xml'
        violations[0].lineNumber == 35
    }

}
