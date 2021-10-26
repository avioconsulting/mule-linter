package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ConfigPlaceholderRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addFile('src/main/mule/global.xml', GLOBAL)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Global Config tested attributes without placeholders fail rule'() {
        given:
        Rule rule = new ConfigPlaceholderRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 9
        violations[0].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'key'
        violations[1].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'host'
        violations[2].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[3].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[4].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'keyPassword'
        violations[5].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[6].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'host'
        violations[7].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'username'
        violations[8].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'username'
    }

    def 'Global Config tested attributes without placeholders fail rule less with custom list'() {
        given:
        String[] placeholderAttributes = ['password', 'key', 'host', 'nonProxyHosts', 'keyPassword']
        Rule rule = new ConfigPlaceholderRule(placeholderAttributes)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 8
        violations[0].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'key'
        violations[1].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'host'
        violations[2].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[3].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[4].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'keyPassword'
        violations[5].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'nonProxyHosts'
        violations[6].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'password'
        violations[7].message == ConfigPlaceholderRule.RULE_VIOLATION_MESSAGE + 'host'
    }

    private static final String GLOBAL = '''
<mule xmlns:tls="http://www.mulesoft.org/schema/mule/tls"
\txmlns:http="http://www.mulesoft.org/schema/mule/http" 
\txmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" 
\txmlns="http://www.mulesoft.org/schema/mule/core" 
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
\txsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd">
\t<secure-properties:config name="Bad_Secure_Properties_Config" doc:name="Bad Secure Properties Config" doc:id="a1c9caea-f40f-4e4a-9b6d-5b48e086729e" file="fake.properties" key="someKey"/>
\t<http:request-config name="Bad_HTTP_Request_configuration" doc:name="Bad HTTP Request configuration" doc:id="8b39c765-8f03-4308-9b69-706f9790943e" >
\t\t<http:request-connection host="localhost" port="443">
\t\t\t<tls:context >
\t\t\t\t<tls:trust-store path="path/to/truststore" password="password" />
\t\t\t\t<tls:key-store keyPassword="password" password="password" />
\t\t\t</tls:context>
\t\t\t<http:proxy-config >
\t\t\t\t<http:proxy host="localhost" port="443" username="username" password="password" nonProxyHosts="myexample.com"/>
\t\t\t</http:proxy-config>
\t\t\t<http:authentication >
\t\t\t\t<http:basic-authentication username="username" password="${http.password}" />
\t\t\t</http:authentication>
\t\t</http:request-connection>
\t</http:request-config>
</mule>'''
}
