package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class DisplayNameRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/main.xml', MAIN_CONFIG)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Components with default display name fail rule'() {
        given:
        Rule rule = new DisplayNameRule()
        Application app = new Application(testApp.appDir)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 4
        violations[0].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'set-payload'
        violations[1].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'set-variable'
        violations[2].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'transform'
        violations[3].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'flow-ref'
    }

    def 'More components with default display name fail rule when setting additional component checks'() {
        given:
        List components = [[name: 'set-payload', namespace: Namespace.CORE, displayName: 'Set Payload'],
                           [name: 'set-variable', namespace: Namespace.CORE, displayName: 'Set Variable'],
                           [name: 'transform', namespace: Namespace.CORE_EE, displayName: 'Transform Message'],
                           [name: 'flow-ref', namespace: Namespace.CORE, displayName: 'Flow Reference'],
                           [name: 'request', namespace: Namespace.HTTP, displayName: 'Request']]
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new DisplayNameRule(components)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 5
        violations[0].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'set-payload'
        violations[1].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'set-variable'
        violations[2].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'transform'
        violations[3].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'flow-ref'
        violations[4].message == DisplayNameRule.RULE_VIOLATION_MESSAGE + 'request'
    }

    private static final String MAIN_CONFIG = '''
<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<http:listener-config name="HTTP_Listener_config1" doc:name="HTTP Listener config" doc:id="c3ec8dee-57bb-4a24-9808-1d4d83524c0e" >
\t\t<http:listener-connection host="0.0.0.0" port="8081" />
\t</http:listener-config>
\t<http:request-config name="HTTP_Request_configuration" doc:name="HTTP Request configuration" doc:id="6b4d1254-df7f-48f9-9a1c-cc1d85c33cb6" >
\t\t<http:request-connection host="localhost" port="8082" />
\t</http:request-config>
\t<flow name="test-flow" doc:id="f24fef40-d81e-4f0c-aecb-278633a29a66" >
\t\t<http:listener doc:name="Listener" doc:id="ce81ad5b-be50-495d-9e64-e87e840a0231" config-ref="HTTP_Listener_config1" path="/"/>
\t\t<flow-ref doc:name="Flow Reference" doc:id="78fdbc1e-cadb-45d7-9169-8c6131dbe726" name="test-sub-flow"/>
\t\t<set-payload value="#[payload]" doc:name="Set Payload" doc:id="13885dcf-7bb8-439e-b486-20fcfc8fba62" />
\t\t<set-variable value="#[payload]" doc:name="Set Variable" doc:id="9b607c57-c6e4-4b85-95ae-fda44e734c82" variableName="test"/>
\t\t<http:request method="GET" doc:name="Request" doc:id="b8576c2b-9bc6-476a-aab6-bfc25fc00a1c" config-ref="HTTP_Request_configuration" path="/"/>
\t\t<ee:transform doc:name="Transform Message" doc:id="9f491a51-2787-4303-91cc-69fd132a103b" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
\tmessage: "Hello World"
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t</flow>
\t<sub-flow name="test-sub-flow" doc:id="e4808133-ce3a-4b9e-9bb2-1a29fbb39ef9" >
\t\t<set-payload value="#[payload]" doc:name="payload" doc:id="41c41f12-929a-48e1-9ae2-ddfdd1c7313e" />
\t\t<set-variable value="#[payload]" doc:name="test" doc:id="a40fbd0d-a74a-4c90-8e62-5b616bd06b85" variableName="test" />
\t\t<http:request method="GET" doc:name="GET localhost:8082/" doc:id="41fc9476-997d-4adb-9d8c-bc76b7d2c001" config-ref="HTTP_Request_configuration" path="/" />
\t\t<ee:transform doc:name="Response Message" doc:id="dc9e1d39-8ed9-4dd3-89e8-b2fb5787b666" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
\tmessage: "Hello World"
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t</sub-flow>
</mule>'''
}
