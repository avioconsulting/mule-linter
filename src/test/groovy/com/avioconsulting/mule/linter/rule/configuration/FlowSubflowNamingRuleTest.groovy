package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class FlowSubflowNamingRuleTest extends Specification {

    private static final String NAMING_APP = 'FlowSubflowNaming'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'cameCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.CAMEL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        !violations.any { it.message.contains('cameCaseFlowName') }
        !violations.any { it.message.contains('cameCaseFlow2') }
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'PascalCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.PASCAL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        !violations.any { it.message.contains('PascalCaseFlowName') }
        !violations.any { it.message.contains('PascalCaseFlowName2') }
        !violations.any { it.message.contains('IEnumerable') }
        !violations.any { it.message.contains('IOStream') }
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'snake_case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.SNAKE_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        !violations.any { it.message.contains('snake_case') }
        !violations.any { it.message.contains('snake_case_2') }
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'kebab-case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(CaseNaming.CaseFormat.KEBAB_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        !violations.any { it.message.contains('kebab-case') }
        !violations.any { it.message.contains('kebab-case-2') }
    }
}