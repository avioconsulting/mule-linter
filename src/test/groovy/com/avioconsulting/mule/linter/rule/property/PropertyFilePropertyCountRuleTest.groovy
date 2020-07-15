package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFilePropertyCountRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'uat', 'prod']
    private static final String NAMING_PATTERN = '${env}.properties'
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addPropertyFiles(['dev.properties',
                                  'test.properties',
                                  'uat.properties',
                                  'prod.properties',
                                  'other.properties'])
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Property File Count mismatch with pattern'() {
        given:
        Rule rule = new PropertyFilePropertyCountRule(ENVS, NAMING_PATTERN)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 5
        violations.size() == 4
        violations[0].message.contains('[dev.properties:4, prod.properties:7, test.properties:6, uat.properties:6]')
    }

    def 'Property File Count matching with pattern'() {
        given:
        Rule rule = new PropertyFilePropertyCountRule(['test', 'uat', 'other'], NAMING_PATTERN)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

}
