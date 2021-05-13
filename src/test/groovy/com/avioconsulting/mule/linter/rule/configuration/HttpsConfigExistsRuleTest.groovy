package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.readme.HttpsConfigExistsRule
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class HttpsConfigExistsRuleTest extends Specification {

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

    def '1 HTTPS configurations with no TLS configuration'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', BAD_CONFIG_1)
        Rule rule = new HttpsConfigExistsRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1 && violations[0].rule.getSeverity().equals(RuleSeverity.BLOCKER)

    }

    def '0 HTTPS configurations incorrectly configured'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GOOD_CONFIG_1)
        Rule rule = new HttpsConfigExistsRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    private static final String GOOD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:tls="http://www.mulesoft.org/schema/mule/tls"
\t  xmlns:jms="http://www.mulesoft.org/schema/mule/jms" xmlns:os="http://www.mulesoft.org/schema/mule/os" xmlns:db="http://www.mulesoft.org/schema/mule/db"
\t  xmlns:mongo="http://www.mulesoft.org/schema/mule/mongo"
\t  xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd
http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/mongo http://www.mulesoft.org/schema/mule/mongo/current/mule-mongo.xsd
http://www.mulesoft.org/schema/mule/db http://www.mulesoft.org/schema/mule/db/current/mule-db.xsd
http://www.mulesoft.org/schema/mule/os http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd
http://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd
http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd">

\t<!-- AVIO Mule Logger configuration -->
\t<avio-core:config name="AVIO_Core_Config" doc:name="AVIO Core Config" doc:id="4e221d01-3161-46ac-8e95-791ff05ce148" app_name="${api.name}" app_version="${api.version}" env="${env}" />

\t<!-- This is the Configuration to make mule always refer to globalErrorHandler defined in error-handling.xml for handling exceptions -->
\t<configuration defaultErrorHandler-ref="globalErrorHandler"/>

\t<!-- This Property is to use in AVIO custom logger to log the current mule application version. Change this everytime when you decided to change this application version in POM. -->
\t<global-property doc:name="Global Property" doc:id="17770a11-4157-4172-973b-72174cc545ad" name="pomVersion" value="1.0.0" />

\t<!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
\t<global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />

\t<!-- This is the configuration to refer and encrypt properties in properties file. -->
\t<secure-properties:config name="Secure_Properties_Config" doc:name="Secure Properties Config" doc:id="d64f0a63-454a-486c-be29-1fcdc9534db5" file="properties/${env}.properties" key="${secureKey}" >
\t\t<secure-properties:encrypt algorithm="Blowfish" />
\t</secure-properties:config>

\t<!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
\t<api-gateway:autodiscovery apiId="${api.apiId}" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="np-store-product-sys-api-main" />

\t<!-- Added to expedite the Bootcamp project archetype -->
\t<global-property doc:name="Global Property" doc:id="0a76d4c0-842d-40b6-9fc5-2f5cf73b0360" name="secureKey" value="password" />

\t<!-- reference artifactId specific properties directory -->
\t<configuration-properties doc:name="Configuration properties" doc:id="0da4297a-cc2b-4d83-8e5d-fde6621077ec" file="properties/${env}.properties" />

\t<!-- Listener references "env" specific properties -->
\t<http:listener-config name="HTTP_Listener_config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e">
\t\t<http:listener-connection host="${http.host}" port="${http.port}">
\t\t</http:listener-connection>
\t</http:listener-config>

\t<mongo:config name="MongoDB_Config" doc:name="MongoDB Config" doc:id="79a85663-f48d-4385-918e-cc1356d94d54" >
\t\t<mongo:connection host="${mongo.host}:${mongo.port}" username="${mongo.user}" password="${mongo.pass}" database="${mongo.db}" />
\t</mongo:config>
\t<db:config name="Database_Config" doc:name="Database Config" doc:id="7dfc615b-8c5a-4c9b-91d0-21e4220acee9" >
\t\t<db:my-sql-connection host="${db.host}" port="${db.port}" user="${db.user}" password="${secure::db.password}" database="${db.database}" />
\t</db:config>
\t<os:config name="ObjectStore_Config" doc:name="ObjectStore Config" doc:id="84b2f595-878a-4a83-a7f5-6e393f8021d0" >
\t\t<os:connection />
\t</os:config>
\t<os:object-store name="Object_store" doc:name="Object store" doc:id="5d1d081b-1a23-470f-afa3-a0e7dd0f347b" persistent="false" entryTtl="1" config-ref="ObjectStore_Config" />
\t<ee:object-store-caching-strategy name="Caching_Strategy" doc:name="Caching Strategy" doc:id="99379b47-e80e-4557-88fe-5b5fc1bf4cb9" objectStore="Object_store" />
\t<jms:config name="JMS_Config" doc:name="JMS Config" doc:id="358cb1a5-5416-4de2-975c-453cedddec9e" >
\t\t<jms:active-mq-connection username="${mq.user}" password="${secure::mq.password}" >
\t\t\t<jms:factory-configuration brokerUrl="${mq.broker.url}" />
\t\t</jms:active-mq-connection>
\t</jms:config>
</mule>

'''
    private static final String BAD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:tls="http://www.mulesoft.org/schema/mule/tls"
\t  xmlns:jms="http://www.mulesoft.org/schema/mule/jms" xmlns:os="http://www.mulesoft.org/schema/mule/os" xmlns:db="http://www.mulesoft.org/schema/mule/db"
\t  xmlns:mongo="http://www.mulesoft.org/schema/mule/mongo"
\t  xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway" xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd
http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/mongo http://www.mulesoft.org/schema/mule/mongo/current/mule-mongo.xsd
http://www.mulesoft.org/schema/mule/db http://www.mulesoft.org/schema/mule/db/current/mule-db.xsd
http://www.mulesoft.org/schema/mule/os http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd
http://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd
http://www.mulesoft.org/schema/mule/tls http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd">

\t<!-- AVIO Mule Logger configuration -->
\t<avio-core:config name="AVIO_Core_Config" doc:name="AVIO Core Config" doc:id="4e221d01-3161-46ac-8e95-791ff05ce148" app_name="${api.name}" app_version="${api.version}" env="${env}" />

\t<!-- This is the Configuration to make mule always refer to globalErrorHandler defined in error-handling.xml for handling exceptions -->
\t<configuration defaultErrorHandler-ref="globalErrorHandler"/>

\t<!-- This Property is to use in AVIO custom logger to log the current mule application version. Change this everytime when you decided to change this application version in POM. -->
\t<global-property doc:name="Global Property" doc:id="17770a11-4157-4172-973b-72174cc545ad" name="pomVersion" value="1.0.0" />

\t<!-- This Property is to use in various properties and Custom Logger to reference the environment (e.g. local, dev, etc) -->
\t<global-property doc:name="Global Property" doc:id="a1fa49c4-d27a-47ab-bd5e-20d5f55721f5" name="env" value="local" />

\t<!-- This is the configuration to refer and encrypt properties in properties file. -->
\t<secure-properties:config name="Secure_Properties_Config" doc:name="Secure Properties Config" doc:id="d64f0a63-454a-486c-be29-1fcdc9534db5" file="properties/${env}.properties" key="${secureKey}" >
\t\t<secure-properties:encrypt algorithm="Blowfish" />
\t</secure-properties:config>

\t<!-- This is the configuration to let this mule app talk to API Manager's corresponding API -->
\t<api-gateway:autodiscovery apiId="${api.apiId}" doc:name="API Autodiscovery" doc:id="64e53ea9-8f3d-4165-8451-fc9e075835f3" flowRef="np-store-product-sys-api-main" />

\t<!-- Added to expedite the Bootcamp project archetype -->
\t<global-property doc:name="Global Property" doc:id="0a76d4c0-842d-40b6-9fc5-2f5cf73b0360" name="secureKey" value="password" />

\t<!-- reference artifactId specific properties directory -->
\t<configuration-properties doc:name="Configuration properties" doc:id="0da4297a-cc2b-4d83-8e5d-fde6621077ec" file="properties/${env}.properties" />

\t<!-- Listener references "env" specific properties -->
\t<http:listener-config name="HTTP_Listener_config" doc:name="HTTP Listener config" doc:id="110e7948-6f84-4387-937e-2728db55929e">
\t\t<http:listener-connection host="${http.host}" port="${http.port}">
\t\t</http:listener-connection>
\t</http:listener-config>

\t<mongo:config name="MongoDB_Config" doc:name="MongoDB Config" doc:id="79a85663-f48d-4385-918e-cc1356d94d54" >
\t\t<mongo:connection host="${mongo.host}:${mongo.port}" username="${mongo.user}" password="${mongo.pass}" database="${mongo.db}" />
\t</mongo:config>
\t<db:config name="Database_Config" doc:name="Database Config" doc:id="7dfc615b-8c5a-4c9b-91d0-21e4220acee9" >
\t\t<db:my-sql-connection host="${db.host}" port="${db.port}" user="${db.user}" password="${secure::db.password}" database="${db.database}" />
\t</db:config>
\t<os:config name="ObjectStore_Config" doc:name="ObjectStore Config" doc:id="84b2f595-878a-4a83-a7f5-6e393f8021d0" >
\t\t<os:connection />
\t</os:config>
\t<os:object-store name="Object_store" doc:name="Object store" doc:id="5d1d081b-1a23-470f-afa3-a0e7dd0f347b" persistent="false" entryTtl="1" config-ref="ObjectStore_Config" />
\t<ee:object-store-caching-strategy name="Caching_Strategy" doc:name="Caching Strategy" doc:id="99379b47-e80e-4557-88fe-5b5fc1bf4cb9" objectStore="Object_store" />
\t<jms:config name="JMS_Config" doc:name="JMS Config" doc:id="358cb1a5-5416-4de2-975c-453cedddec9e" >
\t\t<jms:active-mq-connection username="${mq.user}" password="${secure::mq.password}" >
\t\t\t<jms:factory-configuration brokerUrl="${mq.broker.url}" />
\t\t</jms:active-mq-connection>
\t</jms:config>
\t<http:listener-config name="HTTP_Listener_config2" doc:name="HTTP Listener config" doc:id="2f61d929-2090-4ab8-a7dc-f22992867615" >
\t\t<http:listener-connection protocol="HTTPS" host="0.0.0.0" port="8081" />
\t</http:listener-config>
</mule>

'''

}
