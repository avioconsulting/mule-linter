package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFileNamingRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'prod']
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
        testApp.addPropertyFiles(['prod.properties',
                                  'dev.properties'])
        def customNamingPattern = '${env}.properties'
        Rule rule = new PropertyFileNamingRule(ENVS,
                                               customNamingPattern)

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 1
        violations[0].fileName == 'test.properties'
        violations[0].message.contains(customNamingPattern)
    }

    def 'Property File Naming Rule check default pattern'() {
        given:
        testApp.addPropertyFiles(['sample-mule-app-dev.properties',
                                  'sample-mule-app-test.properties',
                                  'prod.properties',
                                  'other.properties'])
        Rule rule = new PropertyFileNamingRule(ENVS)

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 4
        violations.size() == 1
        violations[0].fileName == 'sample-mule-app-prod.properties'
    }

}
