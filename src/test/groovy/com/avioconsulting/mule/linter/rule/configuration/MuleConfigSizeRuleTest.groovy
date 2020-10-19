package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class MuleConfigSizeRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.buildConfigContent('first.xml', FIRST_CONFIG)
        testApp.buildConfigContent('second.xml', SECOND_CONFIG)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Config file with too many flows fails rule'() {
        given:
        Rule rule = new MuleConfigSizeRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == MuleConfigSizeRule.RULE_VIOLATION_MESSAGE
    }

    def 'Config files fail rule when flow limit decreased'() {
        given:
        Rule rule = new MuleConfigSizeRule(15)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == MuleConfigSizeRule.RULE_VIOLATION_MESSAGE
        violations[1].message == MuleConfigSizeRule.RULE_VIOLATION_MESSAGE
    }

    private static final String FIRST_CONFIG = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<sub-flow name="main-subflow-one" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-two" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-three" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-four" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-five" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-six" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-seven" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-eight" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-nine" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-ten" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-eleven" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-twelve" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-thirteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-fourteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-fifteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-sixteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
</mule>'''

    private static final String SECOND_CONFIG = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<sub-flow name="main-subflow-one" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-two" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-three" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-four" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-five" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-six" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-seven" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-eight" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-nine" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-ten" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-eleven" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-twelve" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-thirteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-fourteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-fifteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-sixteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-seventeen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-eighteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-nineteen" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-twenty" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
\t<sub-flow name="main-subflow-twenty-one" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
</mule>'''
}
