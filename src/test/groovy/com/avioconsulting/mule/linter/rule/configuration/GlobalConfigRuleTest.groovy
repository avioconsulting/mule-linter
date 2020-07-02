package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification


class GlobalConfigRuleTest extends Specification {

    private static final String GLOBALCONFIG_APP = 'np-store-product-sys-api'
    private static final String INCORRECT_GLOBALCONFIG_APP = 'LoggingMuleApp'
    private static final String MISSING_GLOBALCONFIG_APP = 'SampleMuleApp'
    private final List<String> config = ['secure-properties:config','http:listener-config']
    private final List<String> extraConfig = ['config','listener-config']

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Correct global configuration'() {
        given:
        Rule rule = new GlobalConfigRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect global configuration'() {
        given:
        Rule rule = new GlobalConfigRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(INCORRECT_GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message.contains('config')
        violations[1].message.contains('listener-config')
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing global configuration file'() {
        given:
        Rule rule = new GlobalConfigRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(MISSING_GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == GlobalConfigRule.FILE_MISSING_VIOLATION_MESSAGE
    }
}