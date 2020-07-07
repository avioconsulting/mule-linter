package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification


class GlobalConfigRuleTest extends Specification {

    private static final String GLOBALCONFIG_APP = 'GlobalConfigMuleApp'
    private static final String BAD_GLOBALCONFIG_APP = 'BadGlobalConfigMuleApp'
    private static final String MISSING_GLOBALCONFIG_APP = 'BadMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Correct global configuration'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        File appDir = new File(this.class.classLoader.getResource(GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect global configuration default element check'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        File appDir = new File(this.class.classLoader.getResource(BAD_GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].lineNumber == 10
        violations[0].message.contains('listener-config')
        violations[0].fileName.contains('simple-logging-flow.xml')
        violations[1].lineNumber == 9
        violations[1].message.contains('config')
        violations[1].fileName.contains('simple-logging-flow-with-errors.xml')
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Additional global configuration element check'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml',
                    ['listener-config':'http://www.mulesoft.org/schema/mule/http'])

        when:
        File appDir = new File(this.class.classLoader.getResource(BAD_GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 9
        violations[0].message.contains('config')
        violations[0].fileName.contains('simple-logging-flow-with-errors.xml')
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing global configuration file'() {
        given:
        Rule rule = new GlobalConfigRule('global-config.xml')

        when:
        File appDir = new File(this.class.classLoader.getResource(MISSING_GLOBALCONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }
}