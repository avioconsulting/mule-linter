package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification


@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class OnErrorLogExceptionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.create()
        testApp.addPom()
        testApp.addConfig()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Flows are correct'() {
        given:
        Rule rule = new OnErrorLogExceptionRule()

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 3
        violations.size() == 0
    }

    def 'on-error-continue and -propagate invalid'() {
        given:
        Rule rule = new OnErrorLogExceptionRule()

        when:
        testApp.addFile('src/main/mule/on-error-logging-exception.xml', BAD_CONFIG)
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 4
        violations.size() == 2
        violations[0].lineNumber == 23
        violations[0].fileName.contains('on-error-logging-exception.xml')
        violations[1].lineNumber == 69
    }

    private static final String BAD_CONFIG = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
\t\txmlns="http://www.mulesoft.org/schema/mule/core"
\t\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\t\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
\t\txsi:schemaLocation="http://www.mulesoft.org/schema/mule/core
\t\t\thttp://www.mulesoft.org/schema/mule/core/current/mule.xsd
\t\t\thttp://www.mulesoft.org/schema/mule/ee/core
\t\t\thttp://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<sub-flow name="bad-sub-flow">
\t\t<try doc:name="Try" doc:id="246a02ea-45b3-4796-b41d-a57c0384bf7b" >
\t\t\t<ee:transform doc:name="Simple Transform">
\t\t\t<ee:message>
\t\t\t\t<ee:set-payload><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t\t<error-handler >
\t\t\t\t<on-error-continue enableNotifications="true" logException="false"
\t\t\t\t\t\tdoc:name="Bad On Error Continue" >
\t\t\t\t\t<logger level="ERROR" doc:name="Log Error"
\t\t\t\t\t\t\tmessage='An Error Occured... Continuing'
\t\t\t\t\t\t\tcategory="com.avioconsulting.mulelinter"/>
\t\t\t\t</on-error-continue>
\t\t\t</error-handler>
\t\t</try>
\t\t<try doc:name="Try" >
\t\t\t<logger level="DEBUG" doc:name="Log End" message="Ending"
\t\t\t\t\tcategory="com.avioconsulting.mulelinter" />
\t\t\t<error-handler >
\t\t\t\t<on-error-propagate enableNotifications="true"
\t\t\t\t\t\tlogException="true" doc:name="On Error Propagate"
\t\t\t\t\t\tdoc:id="c59d99e4-e679-4f4f-aa10-02541a3428d2" >
\t\t\t\t\t<logger level="ERROR" doc:name="Log Another Error"
\t\t\t\t\t\tmessage='An Error Occured... Propagating'
\t\t\t\t\t\tcategory="com.avioconsulting.mulelinter" />
\t\t\t\t</on-error-propagate>
\t\t\t</error-handler>
\t\t</try>
\t</sub-flow>
\t<sub-flow name="bad-sub-flow-2">
\t\t<try doc:name="Try">
\t\t\t<ee:transform doc:name="Simple Transform">
\t\t\t<ee:message>
\t\t\t\t<ee:set-payload><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t\t<error-handler >
\t\t\t\t<on-error-continue enableNotifications="true" logException="true"
\t\t\t\t\t\tdoc:name="On Error Continue" doc:id="28755785-b5a0-4403-ae4e-71223ac59a78" >
\t\t\t\t\t<logger level="ERROR" doc:name="Log Error"
\t\t\t\t\t\t\tmessage='An Error Occured... Continuing'
\t\t\t\t\t\t\tcategory="com.avioconsulting.mulelinter"/>
\t\t\t\t</on-error-continue>
\t\t\t</error-handler>
\t\t</try>
\t\t<try doc:name="Try">
\t\t\t<logger level="DEBUG" doc:name="Log End" message="Ending"
\t\t\t\t\tcategory="com.avioconsulting.mulelinter" />
\t\t\t<error-handler >
\t\t\t\t<on-error-propagate enableNotifications="true" logException="false"
\t\t\t\t\t\tdoc:name="Bad On Error Propagate" doc:id="dccce6bd-82dd-4223-a0cc-01bbe3f892cf" >
\t\t\t\t\t<logger level="ERROR" doc:name="Log Another Error"
\t\t\t\t\t\tmessage='An Error Occured... Propagating'
\t\t\t\t\t\tcategory="com.avioconsulting.mulelinter" />
\t\t\t\t</on-error-propagate>
\t\t\t</error-handler>
\t\t</try>
\t</sub-flow>
</mule>
'''
}
