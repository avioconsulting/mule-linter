package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class UntilSuccessfulRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'No Until Successful passes rule'() {
        given:
        Rule rule = new UntilSuccessfulRule()

        when:
        testApp.buildConfigContent('business-logic.xml', FIRST_IMPLEMENTATION)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Using Until Successful fails rule'() {
        given:
        Rule rule = new UntilSuccessfulRule()

        when:
        testApp.buildConfigContent('business-logic.xml', SECOND_IMPLEMENTATION)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == "until-successful" + ComponentCountRule.RULE_VIOLATION_MESSAGE
    }


    private static final String FIRST_IMPLEMENTATION = '''
\t<sub-flow name="business-subflow-one" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87cd" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b74992" />
\t</sub-flow>'''

    private static final String SECOND_IMPLEMENTATION = '''
\t<sub-flow name="business-subflow-three" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87ce" >
\t\t<until-successful maxRetries="5" doc:name="Until Successful" doc:id="c5b598ae-1a93-4add-bdfe-ec178a034bfb" >
\t\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a2" />
\t\t</until-successful>
\t</sub-flow>'''
}
