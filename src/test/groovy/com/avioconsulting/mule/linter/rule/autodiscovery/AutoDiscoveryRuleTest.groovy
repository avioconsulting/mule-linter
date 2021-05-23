package com.avioconsulting.mule.linter.rule.autodiscovery

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class AutoDiscoveryRuleTest extends Specification {

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

    def 'API configuration correctly configured with Autodiscovery'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        Rule rule = new AutoDiscoveryRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'API configurations with no Autodiscovery configuration'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_1)
        Rule rule = new AutoDiscoveryRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1

        violations[0].lineNumber==0
        violations[0].message == AutoDiscoveryRule.MISSING_AUTODISCOVERY_MESSAGE

    }

    def 'Autodiscovery configuration with apiId not externalized'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_2)
        Rule rule = new AutoDiscoveryRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 22
        violations[0].message == AutoDiscoveryRule.AUTODISCOVERY_NOT_EXTERNALIZED_MESSAGE+AutoDiscoveryRule.DEFAULT_GLOBAL_CONFIG_FILE_NAME+". The Hard coded value for API ID is : 123"

    }

    def 'Global Configuration File Not available'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_1)
        Rule rule = new AutoDiscoveryRule("global-config-unknown.xml")

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1

        violations[0].lineNumber==0
        violations[0].message == AutoDiscoveryRule.FILE_MISSING_VIOLATION_MESSAGE

    }


    private static final String GOOD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:file="http://www.mulesoft.org/schema/mule/file" xmlns:sqs="http://www.mulesoft.org/schema/mule/sqs" xmlns:os="http://www.mulesoft.org/schema/mule/os" xmlns:vm="http://www.mulesoft.org/schema/mule/vm" xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd   http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd   http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd   http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd   http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/os http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd
http://www.mulesoft.org/schema/mule/sqs http://www.mulesoft.org/schema/mule/sqs/current/mule-sqs.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd">
     <!-- AVIO Health Check configuration -->
     <import doc:name="Import" doc:id="13748ced-0740-494e-a895-2aeaac40a6c0" file="health-check-api.xml" /> 
    <!-- AVIO Mule Logger configuration -->
    <avio-core:config name="avio-core-config" doc:name="AVIO Core Config" doc:id="4e221d01-3161-46ac-8e95-791ff05ce148" app_name="${api.name}" app_version="${api.version}" env="${env}" />
    <!-- This is the Configuration to make mule always refer to globalErrorHandler defined in error-handling.xml for handling exceptions -->
    <configuration defaultErrorHandler-ref="globalErrorHandler" />
    <!-- This Property is to use in AVIO custom logger to log the current mule application version. Change this everytime when you decided to change this application version in POM. -->
    <global-property doc:name="Global Property" doc:id="17770a11-4157-4172-973b-72174cc545ad" name="pomVersion" value="1.0.0-SNAPSHOT" />
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <!-- This is the configuration to refer and encrypt properties in properties file. -->
    <secure-properties:config name="secure-properties-config" doc:name="Secure Properties Config" doc:id="d64f0a63-454a-486c-be29-1fcdc9534db5" file="properties/${env}.properties" key="${secureKey}">
        <secure-properties:encrypt algorithm="Blowfish" />
    </secure-properties:config>
    <!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
    <api-gateway:autodiscovery apiId="${api.id}" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="sv-sales-order-api-main" />
    <!-- Added to expedite the Bootcamp project archetype -->
    <global-property doc:name="Global Property" doc:id="0a76d4c0-842d-40b6-9fc5-2f5cf73b0360" name="secureKey" value="password" />
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
    <http:request-config name="http-request-configuration" doc:name="htt_request_configuration" doc:id="77b8e116-5e56-4825-ad2c-72ae00ede9c5">
        <http:request-connection host="${http.host}" port="${https.port}" protocol="HTTPS"/>
    </http:request-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
    <vm:config name="vm-config" doc:name="VM Config" doc:id="0b25a1df-16fb-4138-809e-1645a2e00cea" >
\t\t<vm:connection >
\t\t\t<reconnection >
\t\t\t\t<reconnect />
\t\t\t</reconnection>
\t\t</vm:connection>
\t\t<vm:queues >
\t\t\t<vm:queue queueName="salesOrderInputQ" queueType="PERSISTENT" />
\t\t\t<vm:queue queueName="salesOrderErrorQ" queueType="PERSISTENT" />
\t\t</vm:queues>
\t</vm:config>
\t<os:object-store name="sales-order-status-object-store" doc:name="Object store" doc:id="ac408455-311b-411d-9809-f0fa3ce469d3" entryTtl="5" entryTtlUnit="MINUTES" expirationInterval="5" />
\t<http:request-config name="http-shipping-request-config" doc:name="HTTP Request configuration" doc:id="806e4a60-a3bf-4f31-9545-d45ec29ee5c8" basePath="${shipping.basepath}">
\t\t<http:request-connection host="${shipping.host}">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="10000"/>
\t\t\t</reconnection>
\t\t</http:request-connection>
\t</http:request-config>
\t<http:request-config name="http-scheduling-request-config" doc:name="HTTP Request configuration" doc:id="ec917c2b-5821-4db3-9546-6bf185443c24" basePath="/${scheduler.basepath}">
\t\t<http:request-connection host="${scheduler.host}" port="${scheduler.port}"/>
\t</http:request-config>
\t<sqs:config name="amazon-sqs-configuration" doc:name="Amazon SQS Configuration" doc:id="15f349e6-e3e2-4a1d-bac5-b71b463b6973" defaultQueueUrl="${sqs.globalurl}">
\t\t<sqs:basic-connection accessKey="${secure::sqs.accesskey}" secretKey="${secure::sqs.secretkey}" testQueueArn="${sqs.testqueue}" region="us-west-2">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="5000" />
\t\t\t</reconnection>
\t\t</sqs:basic-connection>
\t</sqs:config>
\t<file:config name="csv-file-config" doc:name="File Config" doc:id="699ed3f4-7d0f-4253-99a4-318f0be1f36e" >
\t\t<file:connection workingDir="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples" />
\t</file:config>
\t<error-handler name="globalErrorHandler">
        <on-error-propagate type="APIKIT:BAD_REQUEST" enableNotifications="true" logException="false">
            <set-variable value="400" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_FOUND" enableNotifications="true" logException="false">
            <set-variable value="404" doc:name="Set HTTP Status code" doc:id="5f6f2bd7-a208-4998-a35c-a18eef871ae1" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="8fc5a7ea-ba62-4053-bd85-c2a51cc6cd09" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:METHOD_NOT_ALLOWED" enableNotifications="true" logException="false">
            <set-variable value="405" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_ACCEPTABLE" enableNotifications="true" logException="false">
            <set-variable value="406" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:UNSUPPORTED_MEDIA_TYPE" enableNotifications="true" logException="false">
            <set-variable value="415" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_IMPLEMENTED" enableNotifications="true" logException="false">
            <set-variable value="501" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
         <!-- Added Status code 400 to handle scenarios when Sales Order ID Key is not found in ObjectStore Cache  -->
        <on-error-propagate enableNotifications="true" logException="true" doc:name="On Error Propagate" doc:id="d5da10d5-1a8a-4917-a56b-a27d104c11f7" type="SOCACHE:KEY_NOT_FOUND">
\t\t\t<set-variable value="404" doc:name="Set HTTP Status code" doc:id="abc6fe3b-df2d-4702-9c4c-7c3bf823e66c" variableName="statusCode" />
\t\t\t<flow-ref doc:name="error-handling-subflow" doc:id="cb1500a5-1a98-4ee1-a5dd-42fdd7aa1845" name="error-handling-subflow" />
\t\t</on-error-propagate>
        
        <on-error-propagate enableNotifications="true" logException="false" doc:name="On Error Propagate" doc:id="5d14246f-b613-44f0-9c61-6028e61493a6" type="ANY">
            <set-variable value="500" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
    </error-handler>
    <sub-flow name="error-handling-subflow" doc:id="3b4e3606-24c8-45a0-8b78-c2befbc6224e">
        <ee:transform doc:name="Set Error Payload" doc:id="fe4c4d73-1774-4686-bf07-bb8f735baf1c">
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
output application/json
---
{
\tstatusCode: vars.statusCode,
\tapiInError: p('api.name') ++ "/" ++ p('api.version'),
\tcurrentFlow: vars.currentFlow,
\tcorrelationId: vars.headerAttributes.correlationId,
  \terror: {
  \t\t"type": (error.errorType.namespace ++ ":" ++ error.errorType.identifier as String) default "Not found",
  \t\tdetail: (error.description as String) default "Unhandled Exception"
  \t}
}]]></ee:set-payload>
            </ee:message>
        </ee:transform>
        <avio-core:custom-logger doc:name="Custom logger" doc:id="bb3bbc7a-55b6-4b32-ad2d-e99da03dab4d" config-ref="avio-core-config" correlation_id="#[vars.requestId]" message="Exception Occured" level="ERROR" tracePoint="EXCEPTION" status_code="#[payload.statusCode]" type="#[payload.error.&quot;type&quot;]" detail="#[payload.error.detail]" />
    </sub-flow>
</mule>
'''
    private static final String BAD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:file="http://www.mulesoft.org/schema/mule/file" xmlns:sqs="http://www.mulesoft.org/schema/mule/sqs" xmlns:os="http://www.mulesoft.org/schema/mule/os" xmlns:vm="http://www.mulesoft.org/schema/mule/vm" xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd   http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd   http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd   http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd   http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/os http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd
http://www.mulesoft.org/schema/mule/sqs http://www.mulesoft.org/schema/mule/sqs/current/mule-sqs.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd">
     <!-- AVIO Health Check configuration -->
     <import doc:name="Import" doc:id="13748ced-0740-494e-a895-2aeaac40a6c0" file="health-check-api.xml" /> 
    <!-- AVIO Mule Logger configuration -->
    <avio-core:config name="avio-core-config" doc:name="AVIO Core Config" doc:id="4e221d01-3161-46ac-8e95-791ff05ce148" app_name="${api.name}" app_version="${api.version}" env="${env}" />
    <!-- This is the Configuration to make mule always refer to globalErrorHandler defined in error-handling.xml for handling exceptions -->
    <configuration defaultErrorHandler-ref="globalErrorHandler" />
    <!-- This Property is to use in AVIO custom logger to log the current mule application version. Change this everytime when you decided to change this application version in POM. -->
    <global-property doc:name="Global Property" doc:id="17770a11-4157-4172-973b-72174cc545ad" name="pomVersion" value="1.0.0-SNAPSHOT" />
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <!-- This is the configuration to refer and encrypt properties in properties file. -->
    <secure-properties:config name="secure-properties-config" doc:name="Secure Properties Config" doc:id="d64f0a63-454a-486c-be29-1fcdc9534db5" file="properties/${env}.properties" key="${secureKey}">
        <secure-properties:encrypt algorithm="Blowfish" />
    </secure-properties:config>
    <!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
    <!-- Added to expedite the Bootcamp project archetype -->
    <global-property doc:name="Global Property" doc:id="0a76d4c0-842d-40b6-9fc5-2f5cf73b0360" name="secureKey" value="password" />
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
    <http:request-config name="http-request-configuration" doc:name="htt_request_configuration" doc:id="77b8e116-5e56-4825-ad2c-72ae00ede9c5">
        <http:request-connection host="${http.host}" port="${https.port}" protocol="HTTPS"/>
    </http:request-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
    <vm:config name="vm-config" doc:name="VM Config" doc:id="0b25a1df-16fb-4138-809e-1645a2e00cea" >
\t\t<vm:connection >
\t\t\t<reconnection >
\t\t\t\t<reconnect />
\t\t\t</reconnection>
\t\t</vm:connection>
\t\t<vm:queues >
\t\t\t<vm:queue queueName="salesOrderInputQ" queueType="PERSISTENT" />
\t\t\t<vm:queue queueName="salesOrderErrorQ" queueType="PERSISTENT" />
\t\t</vm:queues>
\t</vm:config>
\t<os:object-store name="sales-order-status-object-store" doc:name="Object store" doc:id="ac408455-311b-411d-9809-f0fa3ce469d3" entryTtl="5" entryTtlUnit="MINUTES" expirationInterval="5" />
\t<http:request-config name="http-shipping-request-config" doc:name="HTTP Request configuration" doc:id="806e4a60-a3bf-4f31-9545-d45ec29ee5c8" basePath="${shipping.basepath}">
\t\t<http:request-connection host="${shipping.host}">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="10000"/>
\t\t\t</reconnection>
\t\t</http:request-connection>
\t</http:request-config>
\t<http:request-config name="http-scheduling-request-config" doc:name="HTTP Request configuration" doc:id="ec917c2b-5821-4db3-9546-6bf185443c24" basePath="/${scheduler.basepath}">
\t\t<http:request-connection host="${scheduler.host}" port="${scheduler.port}"/>
\t</http:request-config>
\t<sqs:config name="amazon-sqs-configuration" doc:name="Amazon SQS Configuration" doc:id="15f349e6-e3e2-4a1d-bac5-b71b463b6973" defaultQueueUrl="${sqs.globalurl}">
\t\t<sqs:basic-connection accessKey="${secure::sqs.accesskey}" secretKey="${secure::sqs.secretkey}" testQueueArn="${sqs.testqueue}" region="us-west-2">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="5000" />
\t\t\t</reconnection>
\t\t</sqs:basic-connection>
\t</sqs:config>
\t<file:config name="csv-file-config" doc:name="File Config" doc:id="699ed3f4-7d0f-4253-99a4-318f0be1f36e" >
\t\t<file:connection workingDir="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples" />
\t</file:config>
\t<error-handler name="globalErrorHandler">
        <on-error-propagate type="APIKIT:BAD_REQUEST" enableNotifications="true" logException="false">
            <set-variable value="400" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_FOUND" enableNotifications="true" logException="false">
            <set-variable value="404" doc:name="Set HTTP Status code" doc:id="5f6f2bd7-a208-4998-a35c-a18eef871ae1" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="8fc5a7ea-ba62-4053-bd85-c2a51cc6cd09" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:METHOD_NOT_ALLOWED" enableNotifications="true" logException="false">
            <set-variable value="405" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_ACCEPTABLE" enableNotifications="true" logException="false">
            <set-variable value="406" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:UNSUPPORTED_MEDIA_TYPE" enableNotifications="true" logException="false">
            <set-variable value="415" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_IMPLEMENTED" enableNotifications="true" logException="false">
            <set-variable value="501" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
         <!-- Added Status code 400 to handle scenarios when Sales Order ID Key is not found in ObjectStore Cache  -->
        <on-error-propagate enableNotifications="true" logException="true" doc:name="On Error Propagate" doc:id="d5da10d5-1a8a-4917-a56b-a27d104c11f7" type="SOCACHE:KEY_NOT_FOUND">
\t\t\t<set-variable value="404" doc:name="Set HTTP Status code" doc:id="abc6fe3b-df2d-4702-9c4c-7c3bf823e66c" variableName="statusCode" />
\t\t\t<flow-ref doc:name="error-handling-subflow" doc:id="cb1500a5-1a98-4ee1-a5dd-42fdd7aa1845" name="error-handling-subflow" />
\t\t</on-error-propagate>
        
        <on-error-propagate enableNotifications="true" logException="false" doc:name="On Error Propagate" doc:id="5d14246f-b613-44f0-9c61-6028e61493a6" type="ANY">
            <set-variable value="500" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
    </error-handler>
    <sub-flow name="error-handling-subflow" doc:id="3b4e3606-24c8-45a0-8b78-c2befbc6224e">
        <ee:transform doc:name="Set Error Payload" doc:id="fe4c4d73-1774-4686-bf07-bb8f735baf1c">
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
output application/json
---
{
\tstatusCode: vars.statusCode,
\tapiInError: p('api.name') ++ "/" ++ p('api.version'),
\tcurrentFlow: vars.currentFlow,
\tcorrelationId: vars.headerAttributes.correlationId,
  \terror: {
  \t\t"type": (error.errorType.namespace ++ ":" ++ error.errorType.identifier as String) default "Not found",
  \t\tdetail: (error.description as String) default "Unhandled Exception"
  \t}
}]]></ee:set-payload>
            </ee:message>
        </ee:transform>
        <avio-core:custom-logger doc:name="Custom logger" doc:id="bb3bbc7a-55b6-4b32-ad2d-e99da03dab4d" config-ref="avio-core-config" correlation_id="#[vars.requestId]" message="Exception Occured" level="ERROR" tracePoint="EXCEPTION" status_code="#[payload.statusCode]" type="#[payload.error.&quot;type&quot;]" detail="#[payload.error.detail]" />
    </sub-flow>
</mule>

'''
    private static final String BAD_CONFIG_2 = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:file="http://www.mulesoft.org/schema/mule/file" xmlns:sqs="http://www.mulesoft.org/schema/mule/sqs" xmlns:os="http://www.mulesoft.org/schema/mule/os" xmlns:vm="http://www.mulesoft.org/schema/mule/vm" xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" xmlns:tls="http://www.mulesoft.org/schema/mule/tls" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation=" http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd   http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd   http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd   http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd   http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/os http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd
http://www.mulesoft.org/schema/mule/sqs http://www.mulesoft.org/schema/mule/sqs/current/mule-sqs.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd">
     <!-- AVIO Health Check configuration -->
     <import doc:name="Import" doc:id="13748ced-0740-494e-a895-2aeaac40a6c0" file="health-check-api.xml" /> 
    <!-- AVIO Mule Logger configuration -->
    <avio-core:config name="avio-core-config" doc:name="AVIO Core Config" doc:id="4e221d01-3161-46ac-8e95-791ff05ce148" app_name="${api.name}" app_version="${api.version}" env="${env}" />
    <!-- This is the Configuration to make mule always refer to globalErrorHandler defined in error-handling.xml for handling exceptions -->
    <configuration defaultErrorHandler-ref="globalErrorHandler" />
    <!-- This Property is to use in AVIO custom logger to log the current mule application version. Change this everytime when you decided to change this application version in POM. -->
    <global-property doc:name="Global Property" doc:id="17770a11-4157-4172-973b-72174cc545ad" name="pomVersion" value="1.0.0-SNAPSHOT" />
    <!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
    <global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />
    <!-- This is the configuration to refer and encrypt properties in properties file. -->
    <secure-properties:config name="secure-properties-config" doc:name="Secure Properties Config" doc:id="d64f0a63-454a-486c-be29-1fcdc9534db5" file="properties/${env}.properties" key="${secureKey}">
        <secure-properties:encrypt algorithm="Blowfish" />
    </secure-properties:config>
    <!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
    <api-gateway:autodiscovery apiId="123" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="sv-sales-order-api-main" />
    <!-- Added to expedite the Bootcamp project archetype -->
    <global-property doc:name="Global Property" doc:id="0a76d4c0-842d-40b6-9fc5-2f5cf73b0360" name="secureKey" value="password" />
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
    <http:request-config name="http-request-configuration" doc:name="htt_request_configuration" doc:id="77b8e116-5e56-4825-ad2c-72ae00ede9c5">
        <http:request-connection host="${http.host}" port="${https.port}" protocol="HTTPS"/>
    </http:request-config>
    <apikit:config name="Router" api="sv-sales-order-api.raml" raml="api/sv-sales-order-api.raml" outboundHeadersMapName="outboundHeaders" httpStatusVarName="statusCode" />
    <vm:config name="vm-config" doc:name="VM Config" doc:id="0b25a1df-16fb-4138-809e-1645a2e00cea" >
\t\t<vm:connection >
\t\t\t<reconnection >
\t\t\t\t<reconnect />
\t\t\t</reconnection>
\t\t</vm:connection>
\t\t<vm:queues >
\t\t\t<vm:queue queueName="salesOrderInputQ" queueType="PERSISTENT" />
\t\t\t<vm:queue queueName="salesOrderErrorQ" queueType="PERSISTENT" />
\t\t</vm:queues>
\t</vm:config>
\t<os:object-store name="sales-order-status-object-store" doc:name="Object store" doc:id="ac408455-311b-411d-9809-f0fa3ce469d3" entryTtl="5" entryTtlUnit="MINUTES" expirationInterval="5" />
\t<http:request-config name="http-shipping-request-config" doc:name="HTTP Request configuration" doc:id="806e4a60-a3bf-4f31-9545-d45ec29ee5c8" basePath="${shipping.basepath}">
\t\t<http:request-connection host="${shipping.host}">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="10000"/>
\t\t\t</reconnection>
\t\t</http:request-connection>
\t</http:request-config>
\t<http:request-config name="http-scheduling-request-config" doc:name="HTTP Request configuration" doc:id="ec917c2b-5821-4db3-9546-6bf185443c24" basePath="/${scheduler.basepath}">
\t\t<http:request-connection host="${scheduler.host}" port="${scheduler.port}"/>
\t</http:request-config>
\t<sqs:config name="amazon-sqs-configuration" doc:name="Amazon SQS Configuration" doc:id="15f349e6-e3e2-4a1d-bac5-b71b463b6973" defaultQueueUrl="${sqs.globalurl}">
\t\t<sqs:basic-connection accessKey="${secure::sqs.accesskey}" secretKey="${secure::sqs.secretkey}" testQueueArn="${sqs.testqueue}" region="us-west-2">
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="5000" />
\t\t\t</reconnection>
\t\t</sqs:basic-connection>
\t</sqs:config>
\t<file:config name="csv-file-config" doc:name="File Config" doc:id="699ed3f4-7d0f-4253-99a4-318f0be1f36e" >
\t\t<file:connection workingDir="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples" />
\t</file:config>
\t<error-handler name="globalErrorHandler">
        <on-error-propagate type="APIKIT:BAD_REQUEST" enableNotifications="true" logException="false">
            <set-variable value="400" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_FOUND" enableNotifications="true" logException="false">
            <set-variable value="404" doc:name="Set HTTP Status code" doc:id="5f6f2bd7-a208-4998-a35c-a18eef871ae1" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="8fc5a7ea-ba62-4053-bd85-c2a51cc6cd09" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:METHOD_NOT_ALLOWED" enableNotifications="true" logException="false">
            <set-variable value="405" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_ACCEPTABLE" enableNotifications="true" logException="false">
            <set-variable value="406" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:UNSUPPORTED_MEDIA_TYPE" enableNotifications="true" logException="false">
            <set-variable value="415" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
        <on-error-propagate type="APIKIT:NOT_IMPLEMENTED" enableNotifications="true" logException="false">
            <set-variable value="501" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
         <!-- Added Status code 400 to handle scenarios when Sales Order ID Key is not found in ObjectStore Cache  -->
        <on-error-propagate enableNotifications="true" logException="true" doc:name="On Error Propagate" doc:id="d5da10d5-1a8a-4917-a56b-a27d104c11f7" type="SOCACHE:KEY_NOT_FOUND">
\t\t\t<set-variable value="404" doc:name="Set HTTP Status code" doc:id="abc6fe3b-df2d-4702-9c4c-7c3bf823e66c" variableName="statusCode" />
\t\t\t<flow-ref doc:name="error-handling-subflow" doc:id="cb1500a5-1a98-4ee1-a5dd-42fdd7aa1845" name="error-handling-subflow" />
\t\t</on-error-propagate>
        
        <on-error-propagate enableNotifications="true" logException="false" doc:name="On Error Propagate" doc:id="5d14246f-b613-44f0-9c61-6028e61493a6" type="ANY">
            <set-variable value="500" doc:name="Set HTTP Status code" doc:id="1d6586fd-fb35-42e5-8bdc-3c21840bba73" variableName="statusCode" />
            <flow-ref doc:name="error-handling-subflow" doc:id="b795ed54-98fd-4265-ba7a-a0a484971f96" name="error-handling-subflow" />
        </on-error-propagate>
    </error-handler>
    <sub-flow name="error-handling-subflow" doc:id="3b4e3606-24c8-45a0-8b78-c2befbc6224e">
        <ee:transform doc:name="Set Error Payload" doc:id="fe4c4d73-1774-4686-bf07-bb8f735baf1c">
            <ee:message>
                <ee:set-payload><![CDATA[%dw 2.0
output application/json
---
{
\tstatusCode: vars.statusCode,
\tapiInError: p('api.name') ++ "/" ++ p('api.version'),
\tcurrentFlow: vars.currentFlow,
\tcorrelationId: vars.headerAttributes.correlationId,
  \terror: {
  \t\t"type": (error.errorType.namespace ++ ":" ++ error.errorType.identifier as String) default "Not found",
  \t\tdetail: (error.description as String) default "Unhandled Exception"
  \t}
}]]></ee:set-payload>
            </ee:message>
        </ee:transform>
        <avio-core:custom-logger doc:name="Custom logger" doc:id="bb3bbc7a-55b6-4b32-ad2d-e99da03dab4d" config-ref="avio-core-config" correlation_id="#[vars.requestId]" message="Exception Occured" level="ERROR" tracePoint="EXCEPTION" status_code="#[payload.statusCode]" type="#[payload.error.&quot;type&quot;]" detail="#[payload.error.detail]" />
    </sub-flow>
</mule>


'''

}