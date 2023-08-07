package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFilePropertyCountRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'uat', 'prod']
    private static final String NAMING_PATTERN = '${env}.properties'
    private static final String YAML_NAMING_PATTERN = '${env}.yaml'
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addPom()

    }

    def cleanup() {
        testApp.remove()
    }

    def 'Property File Count mismatch with pattern'() {
        given:
        testApp.addPropertyFiles(['dev.properties',
                                  'test.properties',
                                  'uat.properties',
                                  'prod.properties',
                                  'other.properties'])
        Rule rule = new PropertyFilePropertyCountRule()
        rule.environments = ENVS
        rule.pattern = NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 5
        violations.size() == 4
        violations[0].message.contains('[dev.properties:4, prod.properties:7, test.properties:6, uat.properties:6]')
    }

    def 'Property File Count matching with pattern'() {
        given:
        testApp.addPropertyFiles(['dev.properties',
                                  'test.properties',
                                  'uat.properties',
                                  'prod.properties',
                                  'other.properties'])
        Rule rule = new PropertyFilePropertyCountRule()
        rule.environments = ['test', 'uat', 'other']
        rule.pattern = NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'YAML Property File Count matching with pattern'() {
        given:
        testApp.addPropertyFiles(['dev.yaml',
                                  'test.yaml',
                                  'uat.yaml'])
        Rule rule = new PropertyFilePropertyCountRule()
        rule.environments = ["dev",'test', 'uat']
        rule.pattern = YAML_NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'YAML Property File Count mismatch with pattern'() {
        given:
        testApp.addPropertyFiles(['dev.yaml',
                                  'test.yaml',
                                  'other.yaml'])
        Rule rule = new PropertyFilePropertyCountRule()
        rule.environments = ["dev",'test', 'other']
        rule.pattern = YAML_NAMING_PATTERN

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 3
        violations.size() == 3
        violations[0].message.contains('[dev.yaml:6, other.yaml:5, test.yaml:6]')
    }

}
