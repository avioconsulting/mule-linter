package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import spock.lang.Specification

class FlowSubflowComponentCountTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/main.xml', API)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Flow Subflow count Rule Success'() {
        given:
        testApp.buildConfigContent('first-implementation.xml', FIRST_IMPLEMENTATION)
        app = new MuleApplication(testApp.appDir)
        FlowSubflowComponentCountRule rule = new FlowSubflowComponentCountRule()
//        rule.setProperty('maxCount',20)
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Flow Subflow count Rule Failure'() {
        given:
        testApp.buildConfigContent('second-implementation.xml', SECOND_IMPLEMENTATION)
        app = new MuleApplication(testApp.appDir)
        FlowSubflowComponentCountRule rule = new FlowSubflowComponentCountRule()
//        rule.setProperty('maxCount',20)
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'business-subflow-four has more than the defined components count. Components in the flow/sub-flow: 22'
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
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b74994" />
\t</sub-flow>
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb443" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb444" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t</sub-flow>'''

    private static final String SECOND_IMPLEMENTATION = '''
\t<sub-flow name="business-subflow-three" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87ce" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a2" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a3" />
\t</sub-flow>
\t<sub-flow name="business-subflow-four" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1592" >
\t\t<logger level="INFO" doc:name="Logger 1" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 2" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 3" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 4" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 5" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 6" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 7" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 8" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 9" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 10" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 11" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 12" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 13" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 14" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 15" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 16" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 17" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 18" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 19" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 20" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t\t<logger level="INFO" doc:name="Logger 21" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb445" />
\t\t<logger level="INFO" doc:name="Logger 22" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb446" />
\t</sub-flow>'''
}

