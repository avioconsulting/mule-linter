package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class LoggerAttributesRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addConfig()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Logger Attributes check'() {
        given:
        Rule rule = new LoggerAttributesRule()
        rule.setProperty("requiredAttributes",['category', 'message'])

        when:
        testApp.buildConfigContent('logging-flow-with-errors.xml', ERROR_SUB_FLOWS)
        app = new MuleApplication(testApp.appDir)

        List<RuleViolation> violations = rule.execute(app)

        then:
        println violations
        app.configurationFiles.size() == 4
        violations.size() == 4
        violations[0].fileName.endsWith('logging-flow-with-errors.xml')
        violations[0].lineNumber == 23
        violations[0].message == 'Logger attribute is missing: category'
        violations[1].lineNumber == 42
        violations[1].message == 'Logger attribute is missing: message'
        violations[2].lineNumber == 26
        violations[2].message == 'Logger attribute is missing: category'
        violations[3].lineNumber == 43
        violations[3].message == 'Logger attribute is missing: message'
    }

    private static final String ERROR_SUB_FLOWS = '''<mule xmlns:avio-logger="http://www.mulesoft.org/schema/mule/avio-logger"
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-logger http://www.mulesoft.org/schema/mule/avio-logger/current/mule-avio-logger.xsd">
<sub-flow name="a-sub-flow">
<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
<avio-logger:log level="DEBUG" doc:name="Log Start" config-ref="avio-core-logging-config" correlationId="#[correlationId]" message="Starting" category="com.avioconsulting.mulelinter"/>
<ee:transform doc:name="Simple Transform">
<ee:message>
<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
</ee:message>
</ee:transform>
<logger level="DEBUG" doc:name="Log End" message="Ending" />
<avio-logger:log level="DEBUG" config-ref="avio-core-logging-config" correlationId="#[correlationId]"
doc:name="Log End" 
message="Ending" />
</sub-flow>
<sub-flow name="b-sub-flow">
<logger level="WARN" message="Starting 2" doc:name="Log Start" category="com.avioconsulting.mulelinter" />
<avio-logger:log level="WARN" config-ref="avio-core-logging-config" correlationId="#[correlationId]"
message="Starting 2" 
doc:name="Log Start" category="com.avioconsulting.mulelinter" />
<ee:transform doc:name="Another_Simple Transform">
<ee:message >
<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
</ee:message>
</ee:transform>
<logger level="TRACE" doc:name="Log End" category="com.avioconsulting.mulelinter" />
<avio-logger:log level="TRACE" config-ref="avio-core-logging-config" correlationId="#[correlationId]" doc:name="Log End" category="com.avioconsulting.mulelinter" />
</sub-flow>
</mule>
'''
}
