package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification


class GlobalConfigNoFlowsRuleTest extends Specification {
    private static final String CORRECT_CONFIG_APP = 'GlobalConfigMuleApp'
    private static final String INCORRECT_CONFIG_APP = 'BadGlobalConfigMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'No flow subflow in global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule('global-config.xml')

        when:
        File appDir = new File(this.class.classLoader.getResource(CORRECT_CONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'flow subflow in global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule('global-config.xml')

        when:
        File appDir = new File(this.class.classLoader.getResource(INCORRECT_CONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message.contains('a-sub-flow')
        violations[0].lineNumber == 92
        violations[1].message.contains('a-flow')
        violations[1].lineNumber == 108
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing global configuration file'() {
        given:
        Rule rule = new GlobalConfigNoFlowsRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(CORRECT_CONFIG_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == GlobalConfigNoFlowsRule.FILE_MISSING_VIOLATION_MESSAGE
    }
}