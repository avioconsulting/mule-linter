package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification


@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class OnErrorLogExceptionRuleTest extends Specification {

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

    def 'Flows are correct'() {
        given:
        Rule rule = new OnErrorLogExceptionRule()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 3
        violations.size() == 0
    }

    def 'on-error-continue and -propagate invalid'() {
        given:
        Rule rule = new OnErrorLogExceptionRule(includeTryScopes: true)

        when:
        testApp.addFile('src/main/mule/on-error-logging-exception.xml', BAD_CONFIG)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.configurationFiles.size() == 4
        violations.size() == 2
        violations[0].lineNumber == 23
        violations[0].fileName.contains('on-error-logging-exception.xml')
        violations[0].message.contains(OnErrorLogExceptionRule.RULE_VIOLATION_MESSAGE)
        violations[1].lineNumber == 69
    }

    def 'Try handlers are exempt by default including omitted logging attributes'() {
        given:
        testApp.addFile('src/main/mule/try-handlers.xml', BAD_CONFIG)
        expect:
        new OnErrorLogExceptionRule().execute(new MuleApplication(testApp.appDir)).empty
    }

    def 'scope ownership is structural and shared handlers remain checked when referenced by Try'() {
        given:
        testApp.addFile('src/main/mule/scopes.xml', '''
<m:mule xmlns:m="http://www.mulesoft.org/schema/mule/core">
  <m:flow name="processing">
    <m:try>
      <m:try><m:error-handler><m:on-error-continue logException="false"/></m:error-handler></m:try>
      <m:error-handler><m:on-error-propagate/></m:error-handler>
    </m:try>
    <m:try><m:error-handler ref="shared"/></m:try>
    <m:error-handler><m:on-error-propagate logException="false"/></m:error-handler>
  </m:flow>
  <m:error-handler name="shared"><m:on-error-continue/></m:error-handler>
</m:mule>''')
        def application = new MuleApplication(testApp.appDir)
        expect:
        new OnErrorLogExceptionRule().execute(application).size() == 2
        new OnErrorLogExceptionRule(includeTryScopes: true).execute(application).size() == 4
    }

    def 'exceptions match exact file and named handler and cover every branch type'() {
        given:
        testApp.addFile('src/main/mule/scoped.xml', '''<mule xmlns="http://www.mulesoft.org/schema/mule/core">
<error-handler name="shared"><on-error-propagate type="APP:EXPECTED, APP:OTHER" logException="false"/><on-error-propagate type="ANY" logException="false"/></error-handler>
<error-handler name="unrelated"><on-error-propagate type="APP:EXPECTED" logException="false"/></error-handler>
</mule>''')
        def exception = [file: 'scoped.xml', handler: 'shared', errorTypes: ['APP:EXPECTED', 'APP:OTHER'], reason: 'Safe expected-error event']
        def app = new MuleApplication(testApp.appDir)
        expect:
        new OnErrorLogExceptionRule(exceptions: [exception]).execute(app).size() == 2
        new OnErrorLogExceptionRule(exceptions: [exception + [errorTypes: ['APP:EXPECTED']]]).execute(app).size() == 4
        new OnErrorLogExceptionRule(exceptions: [exception + [file: 'other.xml']]).execute(app).size() == 4
    }

    def 'broad or unexplained exception is rejected'() {
        when:
        new OnErrorLogExceptionRule(exceptions: [[file: 'errors.xml', handler: 'shared', errorTypes: types, reason: reason]])
            .execute(new MuleApplication(testApp.appDir))
        then:
        thrown(IllegalArgumentException)
        where:
        types            | reason
        ['ANY']          | 'too broad'
        ['APP:*']        | 'too broad'
        ['APP:EXPECTED'] | ' '
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
\t\t\t\t<on-error-propagate enableNotifications="true"
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
