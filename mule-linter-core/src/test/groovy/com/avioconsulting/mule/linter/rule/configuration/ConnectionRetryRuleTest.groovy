package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.rule.configuration.ConnectionRetryRule
import spock.lang.Specification

class ConnectionRetryRuleTest extends Specification{
    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'HTTP Request with retry success'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITH_RETRY)
        app = new MuleApplication(testApp.appDir)
        Rule rule = new ConnectionRetryRule()
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'HTTP Request-config with retry configured at config-reference success'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITHOUT_RETRY)
        app = new MuleApplication(testApp.appDir)
        Rule rule = new ConnectionRetryRule()
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'HTTP Request without retry failure'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG_WITHOUT_RETRY)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITHOUT_RETRY)
        app = new MuleApplication(testApp.appDir)
        Rule rule = new ConnectionRetryRule()
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'Connection Retry is not configured for component: request'
    }

    def 'Implementation with more components to check for retry success '() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITH_RETRY)
        List components = [[name: 'request', namespace: Namespace.HTTP],
                           [name: 'publish', namespace: Namespace.VM, 'config-ref': 'config'],
                           [name: 'publish-consume', namespace: Namespace.VM, 'config-ref': 'config']]
        app = new MuleApplication(testApp.appDir)
        Rule rule = new ConnectionRetryRule()
        rule.setProperty('components',components)
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Implementation with more components to check for retry configuration at config-ref success '() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITHOUT_RETRY)
        List components = [[name: 'request', namespace: Namespace.HTTP, 'config-ref': 'request-config'],
                           [name: 'publish', namespace: Namespace.VM, 'config-ref': 'config'],
                           [name: 'publish-consume', namespace: Namespace.VM, 'config-ref': 'config']]
        app = new MuleApplication(testApp.appDir)
        Rule rule = new ConnectionRetryRule()
        rule.setProperty('components',components)
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Implementation with more components to check for retry failure'() {
        given:
        testApp.addFile('src/main/mule/global-config.xml', GLOBAL_CONFIG_WITHOUT_RETRY)
        testApp.buildConfigContent('implementation.xml', IMPLEMENTATION_WITHOUT_RETRY)
        List components = [[name: 'request', namespace: Namespace.HTTP, 'config-ref': 'request-config'],
                           [name: 'publish', namespace: Namespace.VM, 'config-ref': 'config '],
                           [name: 'publish-consume', namespace: Namespace.VM, 'config-ref': 'config']]
        app = new MuleApplication(testApp.appDir)
        ConnectionRetryRule rule = new ConnectionRetryRule()
        rule.setProperty('components',components)
        rule.init()

        when:
        def violations = rule.execute(app)

        then:
        violations.size() == 3
        violations[0].message == 'Connection Retry is not configured for component: request'
        violations[1].message == 'Connection Retry is not configured for component: publish'
        violations[2].message == 'Connection Retry is not configured for component: publish-consume'
    }

    private static final String GLOBAL_CONFIG = '''
<mule xmlns:topgolf-core="http://www.mulesoft.org/schema/mule/topgolf-core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway"
\txmlns:vm="http://www.mulesoft.org/schema/mule/vm"
\txmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties"
\txmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/topgolf-core http://www.mulesoft.org/schema/mule/topgolf-core/current/mule-topgolf-core.xsd">
\t<http:listener-config name="tg-apiname-layer-api-http-listener-config">
\t\t<http:listener-connection host="${api.http.host}" port="${api.http.port}" readTimeout="${api.http.timeout}"/>
\t</http:listener-config>
\t<http:request-config name="acme-http-configuration" doc:name="ACME HTTP Requestor" doc:id="0308de47-ae31-4223-9aca-eb36fb19e5e7" basePath="${acme.basepath}" responseTimeout="${acme.responseTimeout}">
\t\t<http:request-connection host="${acme.host}" port="${acme.port}" protocol="${acme.protocol}">
\t\t\t<reconnection>
\t\t\t\t<reconnect frequency="${acme.retry.frequency}" count="${acme.retry.count}" />
\t\t\t</reconnection>
\t\t</http:request-connection>
\t</http:request-config>
\t<vm:config name="vm-config" doc:name="VM Config" doc:id="580fe736-9cca-44b7-822b-74e7571225f1" >
\t\t<vm:connection >
\t\t\t<reconnection >
\t\t\t\t<reconnect frequency="20000" count="3" />
\t\t\t</reconnection>
\t\t</vm:connection>
\t\t<vm:queues >
\t\t\t<vm:queue queueName="vm-queue" />
\t\t</vm:queues>
\t</vm:config>
</mule>
'''
    private static final String GLOBAL_CONFIG_WITHOUT_RETRY = '''
<mule xmlns:topgolf-core="http://www.mulesoft.org/schema/mule/topgolf-core" xmlns:api-gateway="http://www.mulesoft.org/schema/mule/api-gateway"
\txmlns:vm="http://www.mulesoft.org/schema/mule/vm"
\txmlns:secure-properties="http://www.mulesoft.org/schema/mule/secure-properties"
\txmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:http="http://www.mulesoft.org/schema/mule/http"
\txmlns:apikit="http://www.mulesoft.org/schema/mule/mule-apikit" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/secure-properties http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd
http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/topgolf-core http://www.mulesoft.org/schema/mule/topgolf-core/current/mule-topgolf-core.xsd">
\t<http:listener-config name="tg-apiname-layer-api-http-listener-config">
\t\t<http:listener-connection host="${api.http.host}" port="${api.http.port}" readTimeout="${api.http.timeout}"/>
\t</http:listener-config>
\t<http:request-config name="acme-http-configuration" doc:name="ACME HTTP Requestor" doc:id="0308de47-ae31-4223-9aca-eb36fb19e5e7" basePath="${acme.basepath}">
\t\t<http:request-connection host="${acme.host}" port="${acme.port}" protocol="${acme.protocol}">
\t\t</http:request-connection>
\t</http:request-config>
\t<vm:config name="vm-config" doc:name="VM Config" doc:id="580fe736-9cca-44b7-822b-74e7571225f1" >
\t\t<vm:queues >
\t\t\t<vm:queue queueName="vm-queue" />
\t\t</vm:queues>
\t</vm:config>
</mule>
'''
    private static final String IMPLEMENTATION_WITH_RETRY = '''
<mule xmlns:vm="http://www.mulesoft.org/schema/mule/vm" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
\txmlns:topgolf-core="http://www.mulesoft.org/schema/mule/topgolf-core"
\txmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/topgolf-core http://www.mulesoft.org/schema/mule/topgolf-core/current/mule-topgolf-core.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd">
\t<sub-flow name="get-example-records" doc:id="51864a4f-a3ee-4997-bcd5-e4a0cff87030" >
\t\t<topgolf-core:custom-logger doc:id="8afc0e81-7aa7-4d8d-a971-8cb3d298b82a" config-ref="topgolf-core-config" message="START: Get records from ACME system" category="${log.category}.examples" doc:name="START" correlation_id="#[correlationId]" logLocationInfo="true"/>
\t\t<ee:transform doc:name="Set Query Params" doc:id="a1a0026a-53cb-4ee2-8d2a-fedb3cc7081a" >
\t\t\t<ee:message />
\t\t</ee:transform>
\t\t<vm:publish doc:name="Publish" doc:id="080a0e28-311c-4171-8c83-465450452f33" config-ref="vm-config" sendCorrelationId="ALWAYS" responseTimeout="${acme.responseTimeout}" queueName="testQueue">
\t\t\t<reconnect frequency="5000" count="3" />
\t\t</vm:publish>
\t\t<vm:publish-consume doc:name="Publish consume" doc:id="255ac19b-6955-4d87-ad51-1eae63fe8367" config-ref="vm-config" responseTimeout="${acme.responseTimeout}" queueName="testQueue">
\t\t\t<reconnect frequency="5000" count="3" />
\t\t</vm:publish-consume>
\t\t<topgolf-core:custom-logger doc:id="c1bd8339-eddc-400e-863a-fb36abf3f8d5" config-ref="topgolf-core-config" message='#["Calling ACME  GET /records"]' payload="#[write(vars.queryParams,'application/json')]" level="DEBUG" category="${log.category}.examples" doc:name="DEBUG Print query params"  correlation_id="#[correlationId]" logLocationInfo="true" />
\t\t<http:request method="GET" doc:name="GET /records" doc:id="917ba0d8-8ced-4fe8-99b6-69cc6c5031bf" config-ref="acme-http-configuration" path="/records" responseTimeout="${acme.responseTimeout}" sendCorrelationId="ALWAYS" >
\t\t\t<reconnect frequency="5000" count="3" />
\t\t\t<http:query-params ><![CDATA[#[vars.queryParams]]]></http:query-params>
\t\t</http:request>
\t\t<topgolf-core:custom-logger doc:id="27bc1e93-93b2-4aa3-81e3-61a5a830cb6a" config-ref="topgolf-core-config" message='#["Number of records returned: " ++ sizeOf(payload default [])]' category="${log.category}.examples" doc:name="Records Count" correlation_id="#[correlationId]" logLocationInfo="true" />
\t\t<ee:transform doc:name="Set Response" doc:id="97066b0c-52ea-4ee4-854c-ffe0b15d7ca8" >
\t\t\t<ee:message />
\t\t</ee:transform>
\t\t<topgolf-core:custom-logger doc:id="d5eb2fc2-e566-43fe-9a3f-e91426f3fda2" config-ref="topgolf-core-config" message="END: Get records from ACME system" category="${log.category}.examples" doc:name="END" correlation_id="#[correlationId]" logLocationInfo="true" />
\t</sub-flow>
</mule>'''

    private static final String IMPLEMENTATION_WITHOUT_RETRY = '''
<mule xmlns:vm="http://www.mulesoft.org/schema/mule/vm" xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
\txmlns:topgolf-core="http://www.mulesoft.org/schema/mule/topgolf-core"
\txmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/topgolf-core http://www.mulesoft.org/schema/mule/topgolf-core/current/mule-topgolf-core.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd 
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd">
\t<sub-flow name="get-example-records" doc:id="51864a4f-a3ee-4997-bcd5-e4a0cff87030" >
\t\t<topgolf-core:custom-logger doc:id="8afc0e81-7aa7-4d8d-a971-8cb3d298b82a" config-ref="topgolf-core-config" message="START: Get records from ACME system" category="${log.category}.examples" doc:name="START" correlation_id="#[correlationId]" logLocationInfo="true"/>
\t\t<ee:transform doc:name="Set Query Params" doc:id="a1a0026a-53cb-4ee2-8d2a-fedb3cc7081a" >
\t\t\t<ee:message />
\t\t</ee:transform>
\t\t<topgolf-core:custom-logger doc:id="c1bd8339-eddc-400e-863a-fb36abf3f8d5" config-ref="topgolf-core-config" message='#["Calling ACME  GET /records"]' payload="#[write(vars.queryParams,'application/json')]" level="DEBUG" category="${log.category}.examples" doc:name="DEBUG Print query params"  correlation_id="#[correlationId]" logLocationInfo="true" />
\t\t<vm:publish doc:name="Publish" doc:id="080a0e28-311c-4171-8c83-465450452f33" config-ref="vm-config" sendCorrelationId="NEVER" queueName="testQueue"/>
\t\t<vm:publish-consume doc:name="Publish consume" doc:id="255ac19b-6955-4d87-ad51-1eae63fe8367" config-ref="vm-config" queueName="testQueue"/>
\t\t<http:request method="GET" doc:name="GET /records" doc:id="917ba0d8-8ced-4fe8-99b6-69cc6c5031bf" config-ref="acme-http-configuration" path="/records" sendCorrelationId="NEVER" >
\t\t\t<http:query-params ><![CDATA[#[vars.queryParams]]]></http:query-params>
\t\t</http:request>
\t\t<topgolf-core:custom-logger doc:id="27bc1e93-93b2-4aa3-81e3-61a5a830cb6a" config-ref="topgolf-core-config" message='#["Number of records returned: " ++ sizeOf(payload default [])]' category="${log.category}.examples" doc:name="Records Count" correlation_id="#[correlationId]" logLocationInfo="true" />
\t\t<ee:transform doc:name="Set Response" doc:id="97066b0c-52ea-4ee4-854c-ffe0b15d7ca8" >
\t\t\t<ee:message />
\t\t</ee:transform>
\t\t<topgolf-core:custom-logger doc:id="d5eb2fc2-e566-43fe-9a3f-e91426f3fda2" config-ref="topgolf-core-config" message="END: Get records from ACME system" category="${log.category}.examples" doc:name="END" correlation_id="#[correlationId]" logLocationInfo="true" />
\t</sub-flow>
</mule>'''
}
