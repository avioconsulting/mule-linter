package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString',
        'StaticFieldsBeforeInstanceFields'])
class FlowSubflowNamingRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.create()
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