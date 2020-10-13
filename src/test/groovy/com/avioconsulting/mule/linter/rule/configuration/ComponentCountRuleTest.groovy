package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ComponentCountRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.buildConfigContent('first-implementation.xml', FIRST_IMPLEMENTATION)
        testApp.buildConfigContent('second-implementation.xml', SECOND_IMPLEMENTATION)

        app = new Application(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Rule for less than 8 loggers fails'() {
        given:
        Rule rule = new ComponentCountRule("logger", Namespace.CORE, 8)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == "logger" + ComponentCountRule.RULE_VIOLATION_MESSAGE
    }

    def 'Rule for less than 12 loggers passes'() {
        given:
        Rule rule = new ComponentCountRule("logger", Namespace.CORE, 12)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    private static final String API = '''
<mule xmlns="http://www.mulesoft.org/schema/mule/core" 
xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" 
xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" 
xmlns:http="http://www.mulesoft.org/schema/mule/http" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<http:listener-config name="httpListenerConfig">
\t\t<http:listener-connection host="0.0.0.0" port="8081" />
\t</http:listener-config>
\t<apikit:config name="api-config" api="api.raml" raml="api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="httpStatus" />

\t<flow name="api-main">
\t\t<http:listener config-ref="httpListenerConfig" path="/api/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]">
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:response>
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:router config-ref="api-config" />
\t</flow>

\t<flow name="get:\\v1\\widgets:api-config">
\t\t<flow-ref doc:name="get-widgets" doc:id="450de59d-d74f-4df2-aa99-a7a3a3199ead" name="get-widgets" />
\t</flow>
</mule>'''

    private static final String FIRST_IMPLEMENTATION = '''
\t<sub-flow name="business-subflow-one" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87cd" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b74992" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b74993" />
\t</sub-flow>
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb443" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb444" />
\t</sub-flow>'''

    private static final String SECOND_IMPLEMENTATION = '''
\t<sub-flow name="business-subflow-three" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87ce" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a2" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a3" />
\t</sub-flow>
\t<sub-flow name="business-subflow-four" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1592" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t</sub-flow>'''
}
