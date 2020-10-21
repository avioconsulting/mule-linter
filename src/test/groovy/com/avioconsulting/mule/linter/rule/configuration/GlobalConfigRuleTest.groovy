package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class GlobalConfigRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addConfig()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Correct global configuration'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect global configuration default element check'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        testApp.addFile('src/main/mule/bad-global-with-listener.xml', BAD_CONFIG_1)
        testApp.addFile('src/main/mule/bad-global-with-router.xml', BAD_CONFIG_2)
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        RuleViolation rv = violations.find { it.fileName.contains('bad-global-with-router.xml')}
        rv != null
        rv.lineNumber == 9
        rv.message.contains('config')

        RuleViolation rv2 = violations.find { it.fileName.contains('bad-global-with-listener.xml')}
        rv2 != null
        rv2.lineNumber == 10
        rv2.message.contains('listener-config')
    }

    def 'Additional global configuration element check'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml',
                    ['listener-config': Namespace.HTTP])

        when:
        testApp.addFile('src/main/mule/bad-global-with-listener.xml', BAD_CONFIG_1)
        testApp.addFile('src/main/mule/bad-global-with-router.xml', BAD_CONFIG_2)
        testApp.addFile('src/main/mule/global-error-handler.xml', GLOBAL_ERROR_HANDLER_CONFIG)
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 9
        violations[0].message.contains('config')
        violations[0].fileName.contains('bad-global-with-router.xml')
        violations.findAll { it.fileName.endsWith('global-error-handler.xml') }.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing global configuration file'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        testApp.removeFile('src/main/mule/global-config.xml')
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == GlobalConfigRule.FILE_MISSING_VIOLATION_MESSAGE
    }

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
    private static final String BAD_CONFIG_2 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
\thttp://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
\thttp://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<apikit:config outboundHeadersMapName="outboundHeadersMapName" httpStatusVarName="httpStatus" doc:name="Router" doc:id="d898061d-7861-457f-accb-be2ef941b992" name="Router" raml="api/np-store-product-system-api.raml" />
\t<sub-flow name="b-sub-flow">
\t\t<logger level="DEBUG" 
\t\t\tdoc:name="Log End" 
\t\t\tmessage="Ending" />
\t</sub-flow>
</mule>
'''

    private static final String GLOBAL_ERROR_HANDLER_CONFIG = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
\txsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd">
\t<error-handler name="global-error-handler" doc:id="82e9e554-63ed-4a45-bd9e-f3db5ffceca2" >
\t\t<on-error-propagate enableNotifications="true" logException="true" doc:name="On Error Propagate" doc:id="830f0e11-1353-4917-9f0f-a4c233e04700" >
\t\t\t<logger level="ERROR" doc:name="Log Error" doc:id="66c71fbb-dbce-435c-8d22-f9fed6beae53" message="Error!" category="com.avioconsulting.mulelinter"/>
\t\t</on-error-propagate>
\t</error-handler>
</mule>
'''

}
