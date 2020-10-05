package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ComponentAttributeValueRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.buildConfigContent('components-missing-attributes-values.xml', FLOWS)

        app = new Application(testApp.appDir)
    }

    def cleanup() {
        testApp.remove()
    }

    String CORE_NAMESPACE = "http://www.mulesoft.org/schema/mule/core"

    def 'component with required attributes should pass'() {
        given:
        Rule rule = new ComponentAttributeValueRule('example1', CORE_NAMESPACE, ['exists'])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'component with required attribute values should pass'() {
        given:
        Rule rule = new ComponentAttributeValueRule('EXAMPLE2', 'Example 2', 'example2', CORE_NAMESPACE, [exists:~/exists|or_not/])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'component missing required attributes should fail'() {
        given:
        Rule rule = new ComponentAttributeValueRule('EXAMPLE3', 'Example 3', 'example3', CORE_NAMESPACE, ['exists'])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    def 'component with required attributes empty should fail'() {
        given:
        Rule rule = new ComponentAttributeValueRule('EXAMPLE4', 'Example 4', 'example4', CORE_NAMESPACE, ['empty'])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    def 'components missing attributes to match should fail'() {
        given:
        Rule rule = new ComponentAttributeValueRule('EXAMPLE5', 'Example 5', 'example5', CORE_NAMESPACE, [exists:~/exists|or_not/])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    def 'component with wrong attribute values should fail'() {
        given:
        Rule rule = new ComponentAttributeValueRule('EXAMPLE6', 'Example 6', 'example6', CORE_NAMESPACE, [right:~/right/])

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    private static final String FLOWS = '''
\t<flow name="get:\\user\\(id)\\roles:application\\json:my-api-config" doc:id="bcadf69e-4504-4654-9c7a-38971a01ed11" >
\t\t<logger level="INFO" doc:name="Logger" doc:id="6a676a90-4f3a-48c5-90af-358e8c1c298a" />
\t\t<flow-ref doc:name="Flow Reference" doc:id="4bc8fa65-90c4-454f-b5b7-c1c318f3a1cb" name="business-subflow-two"/>
\t</flow>

\t<sub-flow name="business-subflow-two">
\t\t<logger level="DEBUG" doc:name="Log Start" message="Starting" category="com.avioconsulting.mulelinter"/>
\t\t<example1 exists="exists"/>
\t\t<example2 exists="exists"/>
\t\t<example3/>
\t\t<example4 empty=""/>
\t\t<example5/>
\t\t<example6 right="wrong"/>
\t</sub-flow>'''
}
