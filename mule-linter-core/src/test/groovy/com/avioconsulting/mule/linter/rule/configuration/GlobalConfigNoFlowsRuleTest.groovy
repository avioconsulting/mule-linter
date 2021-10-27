package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class GlobalConfigNoFlowsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addConfig()

        app = new MuleApplication(testApp.appDir)
    }

    def 'No flow subflow in global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule('global-config.xml')

        when:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'flow subflow in global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule('global-config.xml')

        when:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_1)
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.contains('a-sub-flow')
        violations[0].lineNumber == 14
    }

    def 'Missing global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule()

        when:
        testApp.removeFile('src/main/mule/global-config.xml')
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == GlobalConfigNoFlowsRule.FILE_MISSING_VIOLATION_MESSAGE
    }

    private static final String GOOD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
\thttp://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
\thttp://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">

\t<http:listener-config name="HTTP_Listener_config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e">
\t\t<http:listener-connection host="${http.host}" port="${http.port}" />
\t</http:listener-config>

</mule>
'''
    private static final String BAD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
\thttp://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
\thttp://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">

\t<http:listener-config name="HTTP_Listener_config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e">
\t\t<http:listener-connection host="${http.host}" port="${http.port}" />
\t</http:listener-config>

\t<sub-flow name="a-sub-flow">
\t\t<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
\t</sub-flow>
</mule>
'''

}