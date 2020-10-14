package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ApiConsoleDisabledRuleTest extends Specification{

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/main.xml', FLOWS)

        app = new Application(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Active flows with API Console should fail'() {
        given:
        Rule rule = new ApiConsoleDisabledRule()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == ApiConsoleDisabledRule.RULE_VIOLATION_MESSAGE
    }

    private static final String FLOWS = '''
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

\t<flow name="api-console">
\t\t<http:listener config-ref="httpListenerConfig" path="/console/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]" />
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:console config-ref="api-config" />
\t</flow>

\t<flow name="api-console-2" initialState="stopped">
\t\t<http:listener config-ref="httpListenerConfig" path="/console/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]" />
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:console config-ref="api-config" />
\t</flow>

\t<flow name="get:\\v1\\widgets:api-config">
\t\t<flow-ref doc:name="get-widgets" doc:id="450de59d-d74f-4df2-aa99-a7a3a3199ead" name="get-widgets" />
\t</flow>
</mule>'''
}
