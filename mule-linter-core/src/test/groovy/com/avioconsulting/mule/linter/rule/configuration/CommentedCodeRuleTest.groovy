package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class CommentedCodeRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/main.xml', COMMENTED_CODE)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Components with default display name fail rule'() {
        given:
        Rule rule = new CommentedCodeRule()
        Application app = new Application(testApp.appDir)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == CommentedCodeRule.RULE_VIOLATION_MESSAGE
        violations[0].lineNumber == 55
    }

    private static final String COMMENTED_CODE = '''
<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:batch="http://www.mulesoft.org/schema/mule/batch"
\txmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/batch http://www.mulesoft.org/schema/mule/batch/current/mule-batch.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
<!-- This comment should be ignored -->
\t<flow name="test-projectFlow1" doc:id="21ba1b20-cd33-45f5-bd8f-e441cbe832b2">
\t\t<scheduler doc:name="Scheduler" doc:id="84a6beb8-3527-48b4-b6b4-d0d034b64dc4">
\t\t\t<scheduling-strategy>
\t\t\t\t<fixed-frequency frequency="100000"/>
\t\t\t</scheduling-strategy>
\t\t</scheduler>
\t\t<set-variable value="#[uuid()]" doc:name="correlationId" doc:id="4e9d78c1-5397-4142-849b-e4f56565f5eb" variableName="correlationId"/>
\t\t<ee:transform doc:name="Transform Message" doc:id="c66d42b2-e953-412d-9d56-ce36ae73cd0c" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
[
\t"A",
\t"B",
\t"C",
\t"D"
]]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t<batch:job jobName="test-projectBatch_Job" doc:id="83e173cf-6cb2-4c55-82f9-630d3310b3ba" maxFailedRecords="-1" jobInstanceId="#[vars.correlationId]" maxConcurrency="8" blockSize="2">
\t\t\t<batch:process-records >
\t\t\t\t<batch:step name="Batch_Step" doc:id="3513cf5a-90fb-44de-a2a2-9bd84fd02a5d" >
\t\t\t\t\t<ee:transform doc:name="Transform Message" doc:id="9ad2323f-1316-48a6-9915-00a565dbb856" >
\t\t\t\t\t\t<ee:message >
\t\t\t\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
"The letter " ++ payload]]></ee:set-payload>
\t\t\t\t\t\t</ee:message>
\t\t\t\t\t</ee:transform>
\t\t\t\t</batch:step>
\t\t\t</batch:process-records>
\t\t\t<batch:on-complete >
\t\t\t\t<ee:transform doc:name="Transform Message" doc:id="3463c10f-2f9f-404b-9670-f3d644f9cadc" >
\t\t\t\t\t<ee:message >
\t\t\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/json
---
payload]]></ee:set-payload>
\t\t\t\t\t</ee:message>
\t\t\t\t</ee:transform>
\t\t\t\t<logger level="INFO" doc:name="Logger" doc:id="3a200506-9e0a-4cb5-9735-4f249d15a6b1" message='#[payload]'/>
\t\t\t</batch:on-complete>
\t\t</batch:job>
\t</flow>
<!-- \t<flow name="static-batch-idFlow" doc:id="e2751f00-c6d0-4fd3-8198-cf536344751f" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="d936ec07-05c2-43d6-be16-1eedb24151fa" />
\t\t<ee:transform doc:name="Transform Message" doc:id="928f50c8-39f2-41a0-bc9b-28c97304e74f" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
-&#45;&#45;
[
\t"Here is the new payload"
]]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t</flow> -->
</mule>
'''
}
