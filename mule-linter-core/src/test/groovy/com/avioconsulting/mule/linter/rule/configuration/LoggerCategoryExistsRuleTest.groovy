package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class LoggerCategoryExistsRuleTest  extends Specification {

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

    def 'Logger Attributes Success'() {
        given:
        Rule rule = new LoggerCategoryExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
        app.configurationFiles.size() == 3
    }

    def 'Logger Attributes Missing Category'() {
        given:
        Rule rule = new LoggerCategoryExistsRule()

        when:
        testApp.buildConfigContent('logging-flow-with-errors.xml', ERROR_SUB_FLOWS)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.endsWith('logging-flow-with-errors.xml')
        violations[0].lineNumber == 20
        app.configurationFiles.size() == 4
    }


    private static final String ERROR_SUB_FLOWS = '''
\t<sub-flow name="a-sub-flow">
\t\t<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
\t\t<ee:transform doc:name="Simple Transform">
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/java
---
{
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t</ee:transform>
\t\t<logger level="DEBUG" 
\t\t\tdoc:name="Log End" 
\t\t\tmessage="Ending" />
\t</sub-flow>
\t<sub-flow name="b-sub-flow">
\t\t<logger level="WARN" 
\t\t\tmessage="Starting 2" 
\t\t\tdoc:name="Log Start" category="com.avioconsulting.mulelinter" />
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
\t</sub-flow>'''
}
