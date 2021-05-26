package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class LoggerStartAndEndOfFlowRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addConfig()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Correct Logger Config'() {
        given:
        Rule rule = new LoggerStartAndEndOfFlowRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def '1 Flow not containing START log'() {
        given:
        testApp.addFile('src/main/mule/business-logic.xml', new File('src/test/resources/NO_START.xml').text)
        Rule rule = new LoggerStartAndEndOfFlowRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    def 'All flows correctly logged'() {
        given:
        testApp.addFile('src/main/mule/business-logic.xml', new File('src/test/resources/VALID_START_END.xml').text)
        Rule rule = new LoggerStartAndEndOfFlowRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

}
