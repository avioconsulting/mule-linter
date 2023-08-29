package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class LoggerMessageExistsRuleTest extends Specification {

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
        Rule rule = new LoggerMessageExistsRule()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 3
        violations.size() == 0
    }

    def 'Logger should fail for missing message attributes'() {
        given:
        Rule rule = new LoggerMessageExistsRule()

        when:
        testApp.buildConfigContent('logging-flow-with-errors.xml', ERROR_SUB_FLOWS)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 4
        violations.size() == 2
        violations[0].fileName.endsWith('logging-flow-with-errors.xml')
        violations[0].lineNumber == 38
        violations[1].lineNumber == 13
    }

    private static final String ERROR_SUB_FLOWS = '''<mule xmlns:avio-logger="http://www.mulesoft.org/schema/mule/avio-logger"
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-logger http://www.mulesoft.org/schema/mule/avio-logger/current/mule-avio-logger.xsd">
\t<sub-flow name="a-sub-flow">
\t\t<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
\t\t<avio-logger:log level="DEBUG" config-ref="avio-core-logging-config" doc:name="Log Start" category="com.avioconsulting.mulelinter"/>
\t\t<ee:transform doc:name="Simple Transform">
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t<logger level="DEBUG" doc:name="Log End" message="Ending" />
\t\t<avio-logger:log level="DEBUG" config-ref="avio-core-logging-config" doc:name="Log End" message="Ending" />
\t</sub-flow>
\t<sub-flow name="b-sub-flow">
\t\t<logger level="WARN" message="Starting 2" doc:name="Log Start" category="com.avioconsulting.mulelinter" />
\t\t<avio-logger:log level="WARN" config-ref="avio-core-logging-config" message="Starting 2" doc:name="Log Start" category="com.avioconsulting.mulelinter" />
\t\t<ee:transform doc:name="Another_Simple Transform">
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t<logger level="TRACE" doc:name="Log End" category="com.avioconsulting.mulelinter" />
\t\t<avio-logger:log level="TRACE" config-ref="avio-core-logging-config" doc:name="Log End" message="Log End" category="com.avioconsulting.mulelinter" />
\t</sub-flow>
</mule>'''
}
