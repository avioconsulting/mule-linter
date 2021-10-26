package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ExcessiveLoggersRuleTest extends Specification{
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
        Rule rule = new ExcessiveLoggersRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == ExcessiveLoggersRule.RULE_VIOLATION_MESSAGE +
                "get:\\user\\(id)\\roles:application\\json:my-api-config"
        violations[1].message == ExcessiveLoggersRule.RULE_VIOLATION_MESSAGE + "business-subflow-three"
    }

    def '3 Count excessive loggers rule fails one flow'() {
        given:
        Rule rule = new ExcessiveLoggersRule(3)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == ExcessiveLoggersRule.RULE_VIOLATION_MESSAGE + "business-subflow-three"
    }

    def 'Custom excessive loggers rule fails no flows'() {
        given:
        EnumMap<LoggerComponent.LogLevel, Integer> excessiveLoggers = [(LoggerComponent.LogLevel.TRACE): 2,
                                                                       (LoggerComponent.LogLevel.DEBUG): 3,
                                                                       (LoggerComponent.LogLevel.INFO): 3,
                                                                       (LoggerComponent.LogLevel.WARN): 2,
                                                                       (LoggerComponent.LogLevel.ERROR): 4]
        Rule rule = new ExcessiveLoggersRule(excessiveLoggers)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    private static final String LOGGERS = '''
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
\t</sub-flow>'''
}
