package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.configuration.CronExpressionConfigurableRule
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])

class CronExpressionConfigurableRuleTest extends Specification{

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

    def 'Cron Expression correctly configured within the property files '() {
        given:
        testApp.addFile('src/main/mule/sales-orders-csv-impl.xml', GOOD_CONFIG_1)
        Rule rule = new CronExpressionConfigurableRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    def 'Cron Expression Hardcoded in the Mule Configuration file'() {
        given:
        testApp.addFile('src/main/mule/sales-orders-csv-impl.xml', BAD_CONFIG_1)
        Rule rule = new CronExpressionConfigurableRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == CronExpressionConfigurableRule.CRON_EXPRESSION_HARD_CODED+"sales-orders-csv-impl.xml"+". The hard coded cron expression is : * * 10 * * * *"
        violations[0].lineNumber == 12

        violations[1].message == CronExpressionConfigurableRule.CRON_EXPRESSION_HARD_CODED+"sales-orders-csv-impl.xml"+". The hard coded cron expression is : * * 12 * * * *"
        violations[1].lineNumber == 84
    }

    private static final String GOOD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
\txmlns:file="http://www.mulesoft.org/schema/mule/file"
\txmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd">
\t<flow name="sales-orders-csv-impl" doc:id="eaafe27a-7c53-40fb-9fef-c34f17bf43a1" >
\t\t<file:listener doc:name="On New or Updated File" doc:id="c590b952-2ac4-4cdc-8f39-df8d9d3d7490" config-ref="csv-file-config" directory="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples/csv" moveToDirectory="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples/csvarchive">
\t\t\t<scheduling-strategy >
\t\t\t\t<cron expression="${cron.expression}" />
\t\t\t</scheduling-strategy>
\t\t\t<file:matcher filenamePattern="*.csv"/>
\t\t</file:listener>
\t\t<avio-core:custom-logger doc:name="START" doc:id="a2cc2e57-9754-4551-984f-66e76644f738" config-ref="avio-core-config" correlation_id="#[attributes.fileName]" message="sales-order-csv-flow" tracePoint="START" category="${logcat}" logLocationInfo="true" />
\t\t<ee:transform doc:name="payload" doc:id="1b3ca8ba-7ec7-423b-a693-5f6cfa8c4a5c" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/json
var orders = payload
---

(orders groupBy ((item, index) -> item.salesOrderId)) pluck ((value, key, index) -> value) 
map (salesOrder, index) -> {
    customerId :    salesOrder[index].customerId,
    customerName :  salesOrder[index].customerName,
    salesOrderId :   salesOrder[index].salesOrderId,
    lineItems : \tsalesOrder map (item) -> {
\t\tlineItemNumber : item.itemNumber,
        quantity : \titem.quantity,
        lineItemType : item.itemType,
        price :\titem.price,
        itemDescription : \titem.itemDescription
    }
    
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t\t<ee:variables >
\t\t\t</ee:variables>
\t\t</ee:transform>
\t\t<foreach doc:name="for-each-sales-order" doc:id="4ba0c498-1766-48a4-9650-bfbca603fc09" >
\t\t\t<try doc:name="Try" doc:id="0e846dae-0429-4fd8-93e3-ad094bbed58d" >
\t\t\t\t<ee:transform doc:name="salesOrderAttributes" doc:id="b6e8d8a1-de3d-4179-b35f-a061ee8cb715">
\t\t\t\t<ee:message>
\t\t\t\t\t<ee:set-payload><![CDATA[%dw 2.0
output application/json
---
payload]]></ee:set-payload>
\t\t\t\t</ee:message>
\t\t\t\t<ee:variables>
\t\t\t\t\t\t<ee:set-variable variableName="salesOrderAttributes"><![CDATA[%dw 2.0
output application/java
---
{
\tsalesOrderStatus:
\t{
\t\tsalesOrderId: payload.salesOrderId,
\t\tstatus: "CONFIRMED"
\t},
\tqueueName: p('vm.inputQ')
}]]></ee:set-variable>
\t\t\t\t</ee:variables>
\t\t\t</ee:transform>
\t\t\t\t<flow-ref doc:name="sr-publish-vm-queue" doc:id="30c62eb1-8669-456c-9e8f-845342e1a696" name="sr-publish-vm-queue" />
\t\t\t\t<flow-ref doc:name="sr-objectstore-sales-order-status-update" doc:id="21c3e1dd-a38f-42bf-b238-d88a0db1a558" name="sr-objectstore-sales-order-status-update" />
\t\t\t\t<error-handler >
\t\t\t\t\t<on-error-continue enableNotifications="true" logException="true" doc:name="On Error Continue" doc:id="c88f283d-02dd-475a-8dfb-09d5e0ccd762" type="ANY">
\t\t\t\t\t\t<avio-core:custom-logger doc:name="ERROR-csv-processing" doc:id="8e23c793-05ca-48a9-9600-2c6f29a241a4" config-ref="avio-core-config" correlation_id="#[payload.salesOrderId]" message="ERROR while processing Sales Order Payload from CSV file" level="ERROR" tracePoint="EXCEPTION" category="${logcat}" status_code="500" type="#[error.errorType.identifier as String]" detail="#[error.description as String]" logLocationInfo="true" />
\t\t\t\t\t</on-error-continue>
\t\t\t\t</error-handler>
\t\t\t</try>
\t\t</foreach>
\t\t<avio-core:custom-logger doc:name="END" doc:id="3470f167-6de9-43a6-bf7f-da431139b96d" config-ref="avio-core-config" message="sales-order-csv-flow" tracePoint="END" category="${logcat}" logLocationInfo="true" correlation_id="#[attributes.fileName]"/>
\t\t<error-handler >
\t\t\t<on-error-continue enableNotifications="true" logException="true" doc:name="On Error Continue" doc:id="87fcf9a0-8d6e-4aba-9fe9-a7a637c19efe" type="ANY">
\t\t\t\t<avio-core:custom-logger doc:name="ERROR CATCH ALL" doc:id="51881ce7-a3c6-428b-94fa-3abeafbeab03" config-ref="avio-core-config" correlation_id="#[payload.salesOrderId]" message="ERROR while processing Sales Order Payload from CSV file" level="ERROR" tracePoint="EXCEPTION" category="${logcat}" status_code="500" type="#[error.errorType.identifier as String]" detail="#[error.description as String]" logLocationInfo="true" />
\t\t\t</on-error-continue>
\t\t</error-handler>
\t</flow>
\t<flow name="sales-orders-csv-implFlow" doc:id="b0c8f50f-11b2-4596-9dc6-cb0e8835814b" >
\t\t<scheduler doc:name="Scheduler Flow" doc:id="404bdd5b-ba0f-4f86-9687-8d047a264175" >
\t\t\t<scheduling-strategy >
\t\t\t\t<cron expression="${cron.expression}" timeZone="CST"/>
\t\t\t</scheduling-strategy>
\t\t</scheduler>
\t</flow>
</mule>


'''


    private static final String BAD_CONFIG_1 = '''<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:avio-core="http://www.mulesoft.org/schema/mule/avio-core" xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
\txmlns:file="http://www.mulesoft.org/schema/mule/file"
\txmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-core http://www.mulesoft.org/schema/mule/avio-core/current/mule-avio-core.xsd">
\t<flow name="sales-orders-csv-impl" doc:id="eaafe27a-7c53-40fb-9fef-c34f17bf43a1" >
\t\t<file:listener doc:name="On New or Updated File" doc:id="c590b952-2ac4-4cdc-8f39-df8d9d3d7490" config-ref="csv-file-config" directory="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples/csv" moveToDirectory="/Users/swapna.venugopal/Documents/Artifacts/AVIO/Training/git-bc-repos/phase3a/bc10-swapna-venugopal-repo/sv-sales-order-api/src/main/resources/examples/csvarchive">
\t\t\t<scheduling-strategy >
\t\t\t\t<cron expression="* * 10 * * * *" />
\t\t\t</scheduling-strategy>
\t\t\t<file:matcher filenamePattern="*.csv"/>
\t\t</file:listener>
\t\t<avio-core:custom-logger doc:name="START" doc:id="a2cc2e57-9754-4551-984f-66e76644f738" config-ref="avio-core-config" correlation_id="#[attributes.fileName]" message="sales-order-csv-flow" tracePoint="START" category="${logcat}" logLocationInfo="true" />
\t\t<ee:transform doc:name="payload" doc:id="1b3ca8ba-7ec7-423b-a693-5f6cfa8c4a5c" >
\t\t\t<ee:message >
\t\t\t\t<ee:set-payload ><![CDATA[%dw 2.0
output application/json
var orders = payload
---

(orders groupBy ((item, index) -> item.salesOrderId)) pluck ((value, key, index) -> value) 
map (salesOrder, index) -> {
    customerId :    salesOrder[index].customerId,
    customerName :  salesOrder[index].customerName,
    salesOrderId :   salesOrder[index].salesOrderId,
    lineItems : \tsalesOrder map (item) -> {
\t\tlineItemNumber : item.itemNumber,
        quantity : \titem.quantity,
        lineItemType : item.itemType,
        price :\titem.price,
        itemDescription : \titem.itemDescription
    }
    
}]]></ee:set-payload>
\t\t\t</ee:message>
\t\t\t<ee:variables >
\t\t\t</ee:variables>
\t\t</ee:transform>
\t\t<foreach doc:name="for-each-sales-order" doc:id="4ba0c498-1766-48a4-9650-bfbca603fc09" >
\t\t\t<try doc:name="Try" doc:id="0e846dae-0429-4fd8-93e3-ad094bbed58d" >
\t\t\t\t<ee:transform doc:name="salesOrderAttributes" doc:id="b6e8d8a1-de3d-4179-b35f-a061ee8cb715">
\t\t\t\t<ee:message>
\t\t\t\t\t<ee:set-payload><![CDATA[%dw 2.0
output application/json
---
payload]]></ee:set-payload>
\t\t\t\t</ee:message>
\t\t\t\t<ee:variables>
\t\t\t\t\t\t<ee:set-variable variableName="salesOrderAttributes"><![CDATA[%dw 2.0
output application/java
---
{
\tsalesOrderStatus:
\t{
\t\tsalesOrderId: payload.salesOrderId,
\t\tstatus: "CONFIRMED"
\t},
\tqueueName: p('vm.inputQ')
}]]></ee:set-variable>
\t\t\t\t</ee:variables>
\t\t\t</ee:transform>
\t\t\t\t<flow-ref doc:name="sr-publish-vm-queue" doc:id="30c62eb1-8669-456c-9e8f-845342e1a696" name="sr-publish-vm-queue" />
\t\t\t\t<flow-ref doc:name="sr-objectstore-sales-order-status-update" doc:id="21c3e1dd-a38f-42bf-b238-d88a0db1a558" name="sr-objectstore-sales-order-status-update" />
\t\t\t\t<error-handler >
\t\t\t\t\t<on-error-continue enableNotifications="true" logException="true" doc:name="On Error Continue" doc:id="c88f283d-02dd-475a-8dfb-09d5e0ccd762" type="ANY">
\t\t\t\t\t\t<avio-core:custom-logger doc:name="ERROR-csv-processing" doc:id="8e23c793-05ca-48a9-9600-2c6f29a241a4" config-ref="avio-core-config" correlation_id="#[payload.salesOrderId]" message="ERROR while processing Sales Order Payload from CSV file" level="ERROR" tracePoint="EXCEPTION" category="${logcat}" status_code="500" type="#[error.errorType.identifier as String]" detail="#[error.description as String]" logLocationInfo="true" />
\t\t\t\t\t</on-error-continue>
\t\t\t\t</error-handler>
\t\t\t</try>
\t\t</foreach>
\t\t<avio-core:custom-logger doc:name="END" doc:id="3470f167-6de9-43a6-bf7f-da431139b96d" config-ref="avio-core-config" message="sales-order-csv-flow" tracePoint="END" category="${logcat}" logLocationInfo="true" correlation_id="#[attributes.fileName]"/>
\t\t<error-handler >
\t\t\t<on-error-continue enableNotifications="true" logException="true" doc:name="On Error Continue" doc:id="87fcf9a0-8d6e-4aba-9fe9-a7a637c19efe" type="ANY">
\t\t\t\t<avio-core:custom-logger doc:name="ERROR CATCH ALL" doc:id="51881ce7-a3c6-428b-94fa-3abeafbeab03" config-ref="avio-core-config" correlation_id="#[payload.salesOrderId]" message="ERROR while processing Sales Order Payload from CSV file" level="ERROR" tracePoint="EXCEPTION" category="${logcat}" status_code="500" type="#[error.errorType.identifier as String]" detail="#[error.description as String]" logLocationInfo="true" />
\t\t\t</on-error-continue>
\t\t</error-handler>
\t</flow>
\t<flow name="sales-orders-csv-implFlow" doc:id="b0c8f50f-11b2-4596-9dc6-cb0e8835814b" >
\t\t<scheduler doc:name="Scheduler Flow" doc:id="404bdd5b-ba0f-4f86-9687-8d047a264175" >
\t\t\t<scheduling-strategy >
\t\t\t\t<cron expression="* * 12 * * * *" timeZone="CST"/>
\t\t\t</scheduling-strategy>
\t\t</scheduler>
\t</flow>
</mule>


'''

}
