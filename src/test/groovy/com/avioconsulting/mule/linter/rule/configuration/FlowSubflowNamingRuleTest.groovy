package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class FlowSubflowNamingRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.buildConfigContent('no-naming-standards.xml', FLOWS)

        app = new Application(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'cameCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.CAMEL_CASE)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 8
        !violations.any { it.message.contains('cameCaseFlowName') }
        !violations.any { it.message.contains('cameCaseFlow2') }
    }

    def 'PascalCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.PASCAL_CASE)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 6
        !violations.any { it.message.contains('PascalCaseFlowName') }
        !violations.any { it.message.contains('PascalCaseFlowName2') }
        !violations.any { it.message.contains('IEnumerable') }
        !violations.any { it.message.contains('IOStream') }
    }

    def 'snake_case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.SNAKE_CASE)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 8
        !violations.any { it.message.contains('snake_case') }
        !violations.any { it.message.contains('snake_case_2') }
    }

    def 'kebab-case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.KEBAB_CASE)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 8
        !violations.any { it.message.contains('kebab-case') }
        !violations.any { it.message.contains('kebab-case-2') }
    }

    private static final String FLOWS = '''
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>

\t<sub-flow name="cameCaseFlowName">
\t\t<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
\t</sub-flow>
\t<flow name="cameCaseFlow2"></flow>

\t<sub-flow name="PascalCaseFlowName"></sub-flow>
\t<flow name="PascalCaseFlowName2"></flow>
\t<sub-flow name="IEnumerable"></sub-flow>
\t<flow name="IOStream"></flow>

\t<sub-flow name="snake_case"></sub-flow>
\t<flow name="snake_case_2"></flow>

\t<sub-flow name="kebab-case"></sub-flow>
\t<flow name="kebab-case-2"></flow>'''
}