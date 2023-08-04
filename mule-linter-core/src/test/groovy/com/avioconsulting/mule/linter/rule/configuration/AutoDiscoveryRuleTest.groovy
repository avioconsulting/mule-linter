package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.configuration.AutoDiscoveryRule
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class AutoDiscoveryRuleTest extends Specification {
    private static final String PROPERTY_DIRECTORY = 'src/main/resources/properties/'
    private static final List<String> ENVS = ['dev', 'test', 'prod']
    private static final String NAMING_PATTERN = '${env}.properties'
    private static final String YAML_NAMING_PATTERN = '${env}.yaml'
    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'API configuration correctly configured with Autodiscovery'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', TEST_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'prod.properties', PROD_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ENVS
        rule.pattern = NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'API configuration correctly configured with Autodiscovery in YAML Property file'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.yaml', YAML_DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.yaml', YAML_TEST_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ['dev', 'test']
        rule.pattern = YAML_NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'Rule to skip API Autodiscovery check'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        Rule rule = new AutoDiscoveryRule()
        rule.enabled = false

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'API configuration with multiple HTTP Listener flows configured with excluded flows for Autodiscovery'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API_WITH_2_HTTP_FLOWS)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', TEST_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'prod.properties', PROD_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ENVS
        rule.pattern = NAMING_PATTERN
        rule.exemptedFlows =['sv-sales-order-api-main-v2']

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'API configurations with no Autodiscovery configuration'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API_WITH_2_HTTP_FLOWS)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', TEST_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'prod.properties', PROD_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ENVS
        rule.pattern = NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1

        violations[0].lineNumber==0
        violations[0].message == AutoDiscoveryRule.MISSING_AUTODISCOVERY_MESSAGE+ "sv-sales-order-api-main-v2"
    }

    def 'Autodiscovery configuration with apiId not externalized'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_2)
        testApp.addFile('src/main/mule/main.xml', API)
        Rule rule = new AutoDiscoveryRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 12
        violations[0].message == AutoDiscoveryRule.API_ID_HARDCODED_VIOLATION_MESSAGE+" The Hard coded value for API ID is : 123"

    }

    def 'Autodiscovery configuration with default values for apiId in property files'() {
        given:
        testApp.addFile('src/main/mule/global.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_DEFAULT_API_ID_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', TEST_DEFAULT_API_ID_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ['dev', 'test']
        rule.pattern = '${env}.properties'

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Autodiscovery configuration with duplicate values for apiId in property files'() {
        given:
        testApp.addFile('src/main/mule/global.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', DEV_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ['dev', 'test']
        rule.pattern = '${env}.properties'

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == "api.id"+AutoDiscoveryRule.DUPLICATE_API_ID_MESSAGE
    }

    def 'Autodiscovery configuration with no value for apiId in property files'() {
        given:
        testApp.addFile('src/main/mule/global.xml', GOOD_CONFIG_1)
        testApp.addFile('src/main/mule/main.xml', API)
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.properties', DEV_PROPERTY)
        testApp.addFile(PROPERTY_DIRECTORY + 'test.properties', NO_API_ID_PROPERTY)
        Rule rule = new AutoDiscoveryRule()
        rule.environments = ['dev', 'test']
        rule.pattern = '${env}.properties'

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == "api.id property is missing in the property file: test.properties"
    }


    private static final String API = '''
<mule xmlns="http://www.mulesoft.org/schema/mule/core" 
xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" 
xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" 
xmlns:http="http://www.mulesoft.org/schema/mule/http" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<flow name="sv-sales-order-api-main">
\t\t<http:listener config-ref="http-listener-config" path="/api/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]">
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:response>
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:router config-ref="api-config" />
\t</flow>

\t<flow name="get:\\v1\\widgets:api-config">
\t\t<flow-ref doc:name="get-widgets" doc:id="450de59d-d74f-4df2-aa99-a7a3a3199ead" name="get-widgets" />
\t</flow>
</mule>'''

    private static final String API_WITH_2_HTTP_FLOWS = '''
<mule xmlns="http://www.mulesoft.org/schema/mule/core" 
xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" 
xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" 
xmlns:http="http://www.mulesoft.org/schema/mule/http" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
\t<flow name="sv-sales-order-api-main">
\t\t<http:listener config-ref="http-listener-config" path="/api/v1/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]">
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:response>
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:router config-ref="api-config" />
\t</flow>
\t<flow name="sv-sales-order-api-main-v2">
\t\t<http:listener config-ref="http-listener-config" path="/api/v2/*">
\t\t\t<http:response statusCode="#[vars.httpStatus default 200]">
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:response>
\t\t\t<http:error-response statusCode="#[vars.httpStatus default 500]">
\t\t\t\t<http:body><![CDATA[#[payload]]]></http:body>
\t\t\t\t<http:headers><![CDATA[#[output application/java
---
{
\t"x-correlation-id" : vars.correlationId
}]]]></http:headers>
\t\t\t</http:error-response>
\t\t</http:listener>
\t\t<apikit:router config-ref="api-config" />
\t</flow>
\t<flow name="get:\\v1\\widgets:api-config">
\t\t<flow-ref doc:name="get-widgets" doc:id="450de59d-d74f-4df2-aa99-a7a3a3199ead" name="get-widgets" />
\t</flow>
</mule>'''

    private static final String GOOD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core" 
xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" 
xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" 
xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" 
xmlns:http="http://www.mulesoft.org/schema/mule/http" 
xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd  
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd">
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <configuration-properties doc:name="Configuration properties" doc:id="8fc9b091-15c5-4485-9ce6-43c05083e8da" file="properties/common.properties" />
    <!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
    <api-gateway:autodiscovery apiId="${api.id}" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="sv-sales-order-api-main" />
    <!-- reference artifactId specific properties directory -->
    <configuration-properties doc:name="Configuration properties" doc:id="0da4297a-cc2b-4d83-8e5d-fde6621077ec" file="properties/${env}.properties" />
    <!-- Listener references "env" specific properties -->
    <http:listener-config name="http-listener-config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e" basePath="${http.router.path}">
        <http:listener-connection host="${http.host}" port="${https.port}" protocol="HTTPS">
            <tls:context>
                <tls:key-store path="${keystore.path}" type="pkcs12" password="${secure::keystore.password}" keyPassword="${secure::keystore.password}" alias="${keystore.alias}" />
            </tls:context>
        </http:listener-connection>
    </http:listener-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
</mule>
'''
    private static final String BAD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd  
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd">
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <configuration-properties doc:name="Configuration properties" doc:id="8fc9b091-15c5-4485-9ce6-43c05083e8da" file="properties/common.properties" />
    <!-- reference artifactId specific properties directory -->
    <configuration-properties doc:name="Configuration properties" doc:id="0da4297a-cc2b-4d83-8e5d-fde6621077ec" file="properties/${env}.properties" />
    <!-- Listener references "env" specific properties -->
    <http:listener-config name="http-listener-config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e" basePath="${http.router.path}">
        <http:listener-connection host="${http.host}" port="${https.port}" protocol="HTTPS">
            <tls:context>
                <tls:key-store path="${keystore.path}" type="pkcs12" password="${secure::keystore.password}" keyPassword="${secure::keystore.password}" alias="${keystore.alias}" />
            </tls:context>
        </http:listener-connection>
    </http:listener-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
</mule>
'''
    private static final String BAD_CONFIG_2 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd 
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd 
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd  
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd  
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd">
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
    <api-gateway:autodiscovery apiId="123" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="sv-sales-order-api-main" />
    <configuration-properties doc:name="Configuration properties" doc:id="8fc9b091-15c5-4485-9ce6-43c05083e8da" file="properties/common.properties" />
    <!-- reference artifactId specific properties directory -->
    <configuration-properties doc:name="Configuration properties" doc:id="0da4297a-cc2b-4d83-8e5d-fde6621077ec" file="properties/${env}.properties" />
    <!-- Listener references "env" specific properties -->
    <http:listener-config name="http-listener-config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e" basePath="${http.router.path}">
        <http:listener-connection host="${http.host}" port="${https.port}" protocol="HTTPS">
            <tls:context>
                <tls:key-store path="${keystore.path}" type="pkcs12" password="${secure::keystore.password}" keyPassword="${secure::keystore.password}" alias="${keystore.alias}" />
            </tls:context>
        </http:listener-connection>
    </http:listener-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
</mule>
'''
    private static final String DEV_PROPERTY = '''
api.id=11111
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String TEST_PROPERTY = '''
api.id=22222
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String PROD_PROPERTY = '''
api.id=3333
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String DEV_DEFAULT_API_ID_PROPERTY = '''
api.id=1
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String TEST_DEFAULT_API_ID_PROPERTY = '''
api.id=0
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String NO_API_ID_PROPERTY = '''
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String YAML_DEV_PROPERTY = '''
api:
  id: 11111
user: jallen
password: "![abcdef==]"
db:
  port: 1521
  host: localhost
  user: areed
  secret: BillsRule!
'''
    private static final String YAML_TEST_PROPERTY = '''
api:
  id: 22222
user: jallen
password: "![abcdef==]"
db:
  port: 1521
  host: localhost
  user: areed
  secret: BillsRule!
'''

}