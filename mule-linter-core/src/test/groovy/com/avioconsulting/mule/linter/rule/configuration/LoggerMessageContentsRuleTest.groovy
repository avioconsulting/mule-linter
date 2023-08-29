package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class LoggerMessageContentsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.buildConfigContent('many-differen-loggers.xml', FLOWS)

        app = new MuleApplication(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Rule with Default configuration, Loggers logging payload should fail'() {
        given:
        Rule rule = new LoggerMessageContentsRule()
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 4
        violations[0].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[1].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[2].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[3].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Loggers logging only the payload in #[payload] pattern should fail'() {
        given:
        Rule rule = new LoggerMessageContentsRule()
        rule.setProperty("pattern",~/#\[payload]/)
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[1].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Loggers logging payload at multiple levels should fail'() {
        given:
        Rule rule = new LoggerMessageContentsRule()
        rule.setProperty("rules",["INFO":~/payload]/, "DEBUG":~/payload]/, "WARN":~/payload]/])
        rule.init()

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 8
        violations[0].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[1].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[2].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[3].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[4].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[5].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[6].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
        violations[7].message.startsWith(LoggerMessageContentsRule.RULE_VIOLATION_MESSAGE)
    }

    private static final String FLOWS = '''<mule xmlns:avio-logger="http://www.mulesoft.org/schema/mule/avio-logger"
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-logger http://www.mulesoft.org/schema/mule/avio-logger/current/mule-avio-logger.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" message="#[payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<avio-logger:log doc:name="Logger" config-ref="avio-core-logging-config" message="#[payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<logger level="WARN" doc:name="Logger" message="#['Improper Value in Payload: ' ++ payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298b" />
\t\t<avio-logger:log level="WARN" doc:name="Logger" config-ref="avio-core-logging-config" message="#['Improper Value in Payload: ' ++ payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298b" />
\t\t<logger level="INFO" doc:name="Logger" message="#['Improper Value in Payload: ' ++ payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298c" />
\t\t<avio-logger:log doc:name="Logger" config-ref="avio-core-logging-config" message="#['Improper Value in Payload: ' ++ payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298c" />
\t\t<logger level="INFO" doc:name="Logger" message="#[payload.cheeseWheels]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298d" />
\t\t<avio-logger:log doc:name="Logger" config-ref="avio-core-logging-config" message="#[payload.cheeseWheels]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298d" />
\t\t<logger level="DEBUG" doc:name="Logger" message="#[payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298e" />
\t\t<avio-logger:log level="DEBUG" config-ref="avio-core-logging-config" doc:name="Logger" message="#[payload]" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298e" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t\t<logger level="INFO" doc:name="Logger" message="#['payload']" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298f" />
\t\t<avio-logger:log doc:name="Logger" config-ref="avio-core-logging-config" message="#['payload']" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298f" />
\t</flow>
</mule>'''
}
