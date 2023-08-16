package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ConsecutiveLoggersCountRuleTest extends Specification{
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.buildConfigContent('business-logic.xml', LOGGERS)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Default excessive loggers rule fails two flows'() {
        given:
        Rule rule = new ConsecutiveLoggersCountRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 3
        violations[0].message == ConsecutiveLoggersCountRule.RULE_VIOLATION_MESSAGE +
                "get:\\user\\(id)\\roles:application\\json:my-api-config"
        violations[1].message == ConsecutiveLoggersCountRule.RULE_VIOLATION_MESSAGE + "business-subflow-three"
        violations[2].message == ConsecutiveLoggersCountRule.RULE_VIOLATION_MESSAGE + "business-subflow-four"
    }

    def '3 Count excessive loggers rule fails one flow'() {
        given:
        Rule rule = new ConsecutiveLoggersCountRule()
        rule.setProperty('excessiveLoggers',3)
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == ConsecutiveLoggersCountRule.RULE_VIOLATION_MESSAGE + "business-subflow-three"
        violations[1].message == ConsecutiveLoggersCountRule.RULE_VIOLATION_MESSAGE + "business-subflow-four"
    }

    def 'Custom excessive loggers rule fails no flows'() {
        given:
        EnumMap<LoggerComponent.LogLevel, Integer> excessiveLoggers = [(LoggerComponent.LogLevel.TRACE): 2,
                                                                       (LoggerComponent.LogLevel.DEBUG): 3,
                                                                       (LoggerComponent.LogLevel.INFO): 3,
                                                                       (LoggerComponent.LogLevel.WARN): 2,
                                                                       (LoggerComponent.LogLevel.ERROR): 4]
        Rule rule = new ConsecutiveLoggersCountRule()
        rule.setProperty('excessiveLoggers',excessiveLoggers)
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    private static final String LOGGERS = '''<mule xmlns:avio-logger="http://www.mulesoft.org/schema/mule/avio-logger"
xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core"
xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd
http://www.mulesoft.org/schema/mule/avio-logger http://www.mulesoft.org/schema/mule/avio-logger/current/mule-avio-logger.xsd">
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298b" />
\t\t<logger level="DEBUG" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298c" />
\t\t<logger level="DEBUG" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298d" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>

\t<sub-flow name="business-subflow-two">
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297a" />
\t\t<logger level="DEBUG" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297b" />
\t\t<logger level="WARN" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297c" />
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297d" />
\t</sub-flow>

\t<sub-flow name="business-subflow-three">
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297a" />
\t\t<logger level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297b" />
\t\t<logger level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297c" />
\t\t<logger level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297d" />
\t</sub-flow>

\t<sub-flow name="business-subflow-four">
\t\t<avio-logger:log doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297a" />
\t\t<avio-logger:log level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297b" />
\t\t<avio-logger:log level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297c" />
\t\t<avio-logger:log level="ERROR" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c297d" />
\t</sub-flow>
</mule>'''
}
