package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFileNamingRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'prod']
    private static final String NAMING_PATTERN = '${appname}.${env}.properties'
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Property File Naming Rule check with pattern'() {
        given:
        testApp.addPropertyFiles(['sample-mule-app.test.properties',
                                  'sample-mule-app.prod.properties',
                                  'dev.properties'])
        Rule rule = new PropertyFileNamingRule(ENVS, NAMING_PATTERN)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 3
        violations.size() == 1
        violations[0].fileName == 'sample-mule-app.dev.properties'
        violations[0].message.contains(NAMING_PATTERN)
    }

    def 'Property File Naming Rule check default pattern'() {
        given:
        testApp.addPropertyFiles(['sample-mule-app-dev.properties',
                                  'sample-mule-app-test.properties',
                                  'prod.properties',
                                  'other.properties'])
        Rule rule = new PropertyFileNamingRule(ENVS)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 4
        violations.size() == 1
        violations[0].fileName == 'sample-mule-app-prod.properties'
    }

}
