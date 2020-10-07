package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class UnusedFlowRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'All flow subflow being used check'() {
        given:
        Rule rule = new UnusedFlowRule()

        when:
        testApp.addFile('src/main/mule/main.xml', MAIN_GOOD)
        testApp.buildConfigContent('business-logic.xml', BUSINESS_GOOD)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Many different connectors indicate usage'() {
        given:
        Rule rule = new UnusedFlowRule()

        when:
        testApp.addFile('src/main/mule/main.xml', MANY_SOURCES)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Some flow subflow not being used check'() {
        given:
        Rule rule = new UnusedFlowRule()

        when:
        testApp.addFile('src/main/mule/main.xml', MAIN_BAD)
        testApp.buildConfigContent('business-logic.xml', BUSINESS_BAD)
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 3
        List<RuleViolation> rv = violations.findAll { it.fileName.contains('main.xml') }
        rv.size() == 2
        rv[0].lineNumber == 6
        rv[0].message.contains('main-flow-one-not-used')
        rv[1].lineNumber == 22
        rv[1].message.contains('main-subflow-one-not-used')


        List<RuleViolation> rv2 = violations.findAll { it.fileName.contains('business-logic.xml') }
        rv2.size() == 1
        rv2[0].lineNumber == 7
        rv2[0].message.contains('business-subflow-one-not-used')
    }

    private static final String BUSINESS_GOOD = '''
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb441" />
\t</sub-flow>'''

    private static final String MAIN_GOOD = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="post:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="main-flow-with-listner" doc:id="92fae7e3-5bc8-40a7-b63e-301fd575a261" >
\t\t<http:listener doc:name="Listener" doc:id="974cb70a-a019-46e1-8935-60e97f46e572" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="ca767d29-cabe-417e-8647-f15549ec6228" />
\t</flow>
</mule>'''

    private static final String BUSINESS_BAD = '''
\t<sub-flow name="business-subflow-one-not-used" doc:id="3ce52020-8918-442f-82f5-de9b0d2b87cd" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="97a8bf4d-167b-4d3b-886b-d75db4b749a2" />
\t</sub-flow>
\t<sub-flow name="business-subflow-two" doc:id="576e233d-92f8-4743-bce2-0d2b7eca1591" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="fdbbf5d7-31cc-411d-a603-14354f4bb441" />
\t</sub-flow>'''

    private static final String MAIN_BAD = '''
<mule xmlns:http="http://www.mulesoft.org/schema/mule/http" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
\t<flow name="main-flow-one-not-used" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="post:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>
\t<flow name="main-flow-with-listner" doc:id="92fae7e3-5bc8-40a7-b63e-301fd575a261" >
\t\t<http:listener doc:name="Listener" doc:id="974cb70a-a019-46e1-8935-60e97f46e572" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="ca767d29-cabe-417e-8647-f15549ec6228" />
\t</flow>
\t<sub-flow name="main-subflow-one-not-used" doc:id="2540f127-14c0-420f-8b6b-c927d0027859" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="8defd657-8224-49a3-b402-b649e9bc04b4" />
\t</sub-flow>
</mule>'''

    private static final String MANY_SOURCES = '''
<mule xmlns:sqs="http://www.mulesoft.org/schema/mule/sqs"
\txmlns:s3="http://www.mulesoft.org/schema/mule/s3"
\txmlns:ldap="http://www.mulesoft.org/schema/mule/ldap" 
\txmlns:anypoint-mq="http://www.mulesoft.org/schema/mule/anypoint-mq" 
\txmlns:workday="http://www.mulesoft.org/schema/mule/workday" 
\txmlns:vm="http://www.mulesoft.org/schema/mule/vm" 
xmlns:salesforce="http://www.mulesoft.org/schema/mule/salesforce" 
xmlns:sftp="http://www.mulesoft.org/schema/mule/sftp" 
xmlns:netsuite="http://www.mulesoft.org/schema/mule/netsuite" 
xmlns:jms="http://www.mulesoft.org/schema/mule/jms" 
xmlns:email="http://www.mulesoft.org/schema/mule/email" 
xmlns:file="http://www.mulesoft.org/schema/mule/file" 
xmlns:ftp="http://www.mulesoft.org/schema/mule/ftp" 
xmlns:http="http://www.mulesoft.org/schema/mule/http" 
xmlns:sockets="http://www.mulesoft.org/schema/mule/sockets" 
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" 
xmlns="http://www.mulesoft.org/schema/mule/core" 
xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/sockets http://www.mulesoft.org/schema/mule/sockets/current/mule-sockets.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/ftp http://www.mulesoft.org/schema/mule/ftp/current/mule-ftp.xsd
http://www.mulesoft.org/schema/mule/file http://www.mulesoft.org/schema/mule/file/current/mule-file.xsd
http://www.mulesoft.org/schema/mule/email http://www.mulesoft.org/schema/mule/email/current/mule-email.xsd
http://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd
http://www.mulesoft.org/schema/mule/netsuite http://www.mulesoft.org/schema/mule/netsuite/current/mule-netsuite.xsd
http://www.mulesoft.org/schema/mule/sftp http://www.mulesoft.org/schema/mule/sftp/current/mule-sftp.xsd
http://www.mulesoft.org/schema/mule/salesforce http://www.mulesoft.org/schema/mule/salesforce/current/mule-salesforce.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/workday http://www.mulesoft.org/schema/mule/workday/current/mule-workday.xsd
http://www.mulesoft.org/schema/mule/anypoint-mq http://www.mulesoft.org/schema/mule/anypoint-mq/current/mule-anypoint-mq.xsd
http://www.mulesoft.org/schema/mule/ldap http://www.mulesoft.org/schema/mule/ldap/current/mule-ldap.xsd
http://www.mulesoft.org/schema/mule/s3 http://www.mulesoft.org/schema/mule/s3/current/mule-s3.xsd
http://www.mulesoft.org/schema/mule/sqs http://www.mulesoft.org/schema/mule/sqs/current/mule-sqs.xsd">
\t<flow name="test-configFlow" doc:id="8bdd5eca-c073-4c3b-976e-63847238e4bc" >
\t\t<sockets:listener doc:name="Listener" doc:id="54510178-624d-4975-8782-3d13e99b259c" />
\t</flow>
\t<flow name="test-configFlow1" doc:id="2cfffbd1-1769-4ab5-9a8a-30c8085101f3" >
\t\t<http:listener doc:name="Listener" doc:id="5b112d0e-6f71-4f84-a7af-5323ee5d0c0c" />
\t</flow>
\t<flow name="test-configFlow2" doc:id="56579d89-c637-4a5d-8f26-23eb51274aad" >
\t\t<ftp:listener doc:name="On New or Updated File" doc:id="70f64a4e-d1ff-4708-b806-cf5779bf28e3" />
\t</flow>
\t<flow name="test-configFlow3" doc:id="6749e0b1-a592-4d5b-9d73-2bae9b0b4d9a" >
\t\t<scheduler doc:name="Scheduler" doc:id="035411c0-1314-4503-a1c8-08ad6dda3584" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</scheduler>
\t</flow>
\t<flow name="test-configFlow4" doc:id="4a074854-5822-4498-9e4f-44e35e861a83" >
\t\t<file:listener doc:name="On New or Updated File" doc:id="51851f0c-ede6-4f7f-9f13-a970fdc62125" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</file:listener>
\t</flow>
\t<flow name="test-configFlow5" doc:id="c2955821-84df-466e-a8de-c736327d1779" >
\t\t<email:listener-imap doc:name="On New Email - IMAP" doc:id="7f03b420-1054-486e-80f2-e33bf242c1b8" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</email:listener-imap>
\t</flow>
\t<flow name="test-configFlow7" doc:id="ae0e6214-fde3-46e1-ba3c-37d624049fb1" >
\t\t<jms:listener doc:name="Listener" doc:id="6e1b09e8-ddd2-4455-85aa-4a8d28b1e506" />
\t</flow>
\t<flow name="test-configFlow6" doc:id="fee252ef-32e6-4729-a038-236cc24418a8" >
\t\t<email:listener-pop3 doc:name="On New Email - POP3" doc:id="e72ffbb0-826d-4c51-8278-716111054856" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</email:listener-pop3>
\t</flow>
\t<flow name="test-configFlow8" doc:id="619098a1-42da-47c6-9cef-87c31a19ce8a" >
\t\t<netsuite:new-object-trigger doc:name="On New Object" doc:id="0a3d841b-4c73-4b0e-b308-f0f451d9bc52" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</netsuite:new-object-trigger>
\t</flow>
\t<flow name="test-configFlow10" doc:id="d0aa8e1d-d7fe-41e8-921e-adf7aef6ae23" >
\t\t<netsuite:deleted-object-trigger doc:name="On Deleted Object" doc:id="3d9ed8c5-d0bd-443b-a363-844fa4f62dc1" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</netsuite:deleted-object-trigger>
\t</flow>
\t<flow name="test-configFlow9" doc:id="0f89a901-fc38-4e3b-89aa-042ccfa9115f" >
\t\t<netsuite:modified-object-trigger doc:name="On Modified Object" doc:id="1bd99b40-087b-497e-bb42-2cd4ab46a7cd" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</netsuite:modified-object-trigger>
\t</flow>
\t<flow name="test-configFlow11" doc:id="f7db58e5-987d-42ca-aaf6-3a70b21350c7">
\t\t<sftp:listener doc:name="On New or Updated File" doc:id="0e49196a-0e33-4be7-9e7f-acfd6bdedd42">
\t\t\t<scheduling-strategy>
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</sftp:listener>
\t</flow>
\t<flow name="test-configFlow12" doc:id="0f9c2872-3f14-4959-8bbe-b745f8ec3f10" >
\t\t<salesforce:new-object doc:name="On New Object" doc:id="1e9550b4-349f-4fea-b6cb-3acfc888199f" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</salesforce:new-object>
\t</flow>
\t<flow name="test-configFlow13" doc:id="a40ad807-d829-405e-b688-8ae9a1016b90" >
\t\t<salesforce:modified-object doc:name="On Modified Object" doc:id="73ca80d3-0fa2-4c75-9b94-d2abadd77ede" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</salesforce:modified-object>
\t</flow>
\t<flow name="test-configFlow14" doc:id="31ee58e2-5c8b-4562-8f29-4fe2fec0b4b7" >
\t\t<salesforce:deleted-object doc:name="On Deleted Object" doc:id="c50bda81-bf9e-47af-9ce6-823af0107f9d" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</salesforce:deleted-object>
\t</flow>
\t<flow name="test-configFlow15" doc:id="c58e6d34-9f7f-4440-aab9-2b276aad59a7">
\t\t<vm:listener doc:name="Listener" doc:id="d1cff502-02d7-4a82-8c22-81cda1d1d0b2" />
\t</flow>
\t<flow name="test-configFlow16" doc:id="20b37651-50c0-4d87-92b6-135a3c79a711" >
\t\t<workday:new-objects-trigger doc:name="On New Objects" doc:id="507ed985-ac93-43a6-9f84-92bde7d4c631" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</workday:new-objects-trigger>
\t</flow>
\t<flow name="test-configFlow17" doc:id="7ce7bbb1-87d6-42ec-9937-92cbc44886ea" >
\t\t<anypoint-mq:subscriber doc:name="Subscriber" doc:id="59b3a5aa-ce64-4306-9924-bcab9d95315b" />
\t</flow>
\t<flow name="test-configFlow18" doc:id="4e337e4e-3457-41fd-aaf3-3b9f6662730c" >
\t\t<ldap:new-objects doc:name="On New Objects" doc:id="502fce1b-d89a-44eb-9dcd-5df4938fda83" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</ldap:new-objects>
\t</flow>
\t<flow name="test-configFlow19" doc:id="153de929-52d9-4894-a68d-b652c1109150" >
\t\t<s3:new-object-trigger doc:name="On New Object" doc:id="f0af4c48-379a-4b58-a2cb-7fa40d905ba7" >
\t\t\t<scheduling-strategy >
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</s3:new-object-trigger>
\t</flow>
\t<flow name="test-configFlow20" doc:id="4bfbbef1-a1ac-44c3-ac6b-ce40a213af02">
\t\t<s3:deleted-object-trigger doc:name="On Deleted Object" doc:id="296505a1-523b-4b7f-a8d1-622f1711709d">
\t\t\t<scheduling-strategy>
\t\t\t\t<fixed-frequency />
\t\t\t</scheduling-strategy>
\t\t</s3:deleted-object-trigger>
\t</flow>
\t<flow name="test-configFlow21" doc:id="7e370ee5-4faf-4768-bf83-e5ca5015abf1" >
\t\t<sqs:receivemessages doc:name="Receive messages" doc:id="3c97eb87-f9b2-4c08-93f5-679739fefc6b" />
\t</flow>
</mule>
'''
}