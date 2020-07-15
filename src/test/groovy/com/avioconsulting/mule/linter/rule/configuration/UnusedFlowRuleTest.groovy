package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class UnusedFlowRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'All flow subflow being used check'() {
        given:
        Rule rule = new UnusedFlowRule()

        when:
        testApp.addFile('src/main/mule/main.xml', MAIN_GOOD)
        testApp.buildConfigContent('business-logic.xml', BUSINESS_GOOD)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Some flow subflow not being used check'() {
        given:
        Rule rule = new UnusedFlowRule()

        when:
        testApp.addFile('src/main/mule/main.xml', MAIN_BAD)
        testApp.buildConfigContent('business-logic.xml', BUSINESS_BAD)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 3
        List<RuleViolation> rv = violations.findAll { it.fileName.contains('main.xml') }
        rv.size() == 2
        rv[0].lineNumber == 6
        rv[0].message.contains('main-flow-one-not-used')
        rv[1].lineNumber == 22
        rv[1].message.contains('main-subflow-one-not-used')


        List<RuleViolation> rv2 = violations.findAll { it.fileName.contains('business-logic.xml') }
        rv2.size() == 1
        rv2[0].lineNumber == 7
        rv2[0].message.contains('business-subflow-one-not-used')
    }

    private static final String BUSINESS_GOOD = '''
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb441" />
\t</sub-flow>'''

    private static final String MAIN_GOOD = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="post:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="main-flow-with-listner" doc:id="92fae7e3-5bc8-40a7-b63e-301fd575a261" >
\t\t<http:listener doc:name="Listener" doc:id="974cb70a-a019-46e1-8935-60e97f46e572" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="ca767d29-cabe-417e-8647-f15549ec6228" />
\t</flow>
</mule>'''

    private static final String BUSINESS_BAD = '''
\t<sub-flow name="business-subflow-one-not-used" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87cd" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a2" />
\t</sub-flow>
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb441" />
\t</sub-flow>'''

    private static final String MAIN_BAD = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="main-flow-one-not-used" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="post:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="main-flow-with-listner" doc:id="92fae7e3-5bc8-40a7-b63e-301fd575a261" >
\t\t<http:listener doc:name="Listener" doc:id="974cb70a-a019-46e1-8935-60e97f46e572" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="ca767d29-cabe-417e-8647-f15549ec6228" />
\t</flow>
\t<sub-flow name="main-subflow-one-not-used" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
</mule>'''

}