package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class PropertyFileNamingRuleTest extends Specification {

    private static final List<String> ENVS = ['dev', 'test', 'prod']
    private static final String NAMING_PATTERN = '${appname}.${env}.properties'
    private static final String APP_NAME = 'SampleMuleApp'

    def 'Property File Naming Rule check with pattern'() {
        given:
        Rule rule = new PropertyFileNamingRule(ENVS, NAMING_PATTERN)

        when:
        File appDir = new File(this.class.classLoader.getResource(application).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size
        violations[0].fileName == 'sample-mule-app.dev.properties'
        violations[0].message.contains(NAMING_PATTERN)

        where:
        application     | size
        APP_NAME        | 1
    }

    def 'Property File Naming Rule check default pattern'() {
        given:
        Rule rule = new PropertyFileNamingRule(ENVS)

        when:
        File appDir = new File(this.class.classLoader.getResource(application).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size
        violations[0].fileName == 'sample-mule-app-prod.properties'

        where:
        application     | size
        APP_NAME        | 1
    }

}
