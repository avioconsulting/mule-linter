package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFilePropertyCountRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'uat', 'prod']
    private static final String NAMING_PATTERN = '${env}.properties'
    private static final String APP_NAME = 'SampleMuleApp'

    def 'Property File Count mismatch with pattern'() {
        given:
        Rule rule = new PropertyFilePropertyCountRule(ENVS, NAMING_PATTERN)

        when:
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 4
        violations[0].message.contains('[uat.properties:6, test.properties:6, dev.properties:4, prod.properties:7]')
    }

    def 'Property File Count matching with pattern'() {
        given:
        Rule rule = new PropertyFilePropertyCountRule(['test', 'uat', 'other'], NAMING_PATTERN)

        when:
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

}
