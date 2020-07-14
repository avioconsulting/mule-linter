package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class ConfigFileNamingRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.addConfig()
        testApp.buildConfigContent('camelCase.xml', '')
        testApp.buildConfigContent('PascalCase.xml', '')
        testApp.buildConfigContent('snake_case.xml', '')
        testApp.buildConfigContent('kebab-case.xml', '')
    }

    def cleanup() {
        testApp.remove()
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'default config file naming convention check'() {
        given:
        Rule rule = new ConfigFileNamingRule()

        expect:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)
        violations.any { it.fileName.contains(FILE_NAME) } == RESULT

        where:
        FILE_NAME        | RESULT
        'camelCase.xml'  | true
        'PascalCase.xml' | true
        'snake_case.xml' | true
        'kebab-case.xml' | false
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'camelCase config file naming convention check'() {
        given:
        Rule rule = new ConfigFileNamingRule(CaseNaming.CaseFormat.CAMEL_CASE)

        expect:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)
        violations.any { it.fileName.contains(FILE_NAME) } == RESULT

        where:
        FILE_NAME        | RESULT
        'camelCase.xml'  | false
        'PascalCase.xml' | true
        'snake_case.xml' | true
        'kebab-case.xml' | true
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'PascalCase config file naming convention check'() {
        given:
        Rule rule = new ConfigFileNamingRule(CaseNaming.CaseFormat.PASCAL_CASE)

        expect:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)
        violations.any { it.fileName.contains(FILE_NAME) } == RESULT

        where:
        FILE_NAME        | RESULT
        'camelCase.xml'  | true
        'PascalCase.xml' | false
        'snake_case.xml' | true
        'kebab-case.xml' | true
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'snake_case config file naming convention check'() {
        given:
        Rule rule = new ConfigFileNamingRule(CaseNaming.CaseFormat.SNAKE_CASE)

        expect:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)
        violations.any { it.fileName.contains(FILE_NAME) } == RESULT

        where:
        FILE_NAME        | RESULT
        'camelCase.xml'  | true
        'PascalCase.xml' | true
        'snake_case.xml' | false
        'kebab-case.xml' | true
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'kebab_case config file naming convention check'() {
        given:
        Rule rule = new ConfigFileNamingRule(CaseNaming.CaseFormat.KEBAB_CASE)

        expect:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)
        violations.any { it.fileName.contains(FILE_NAME) } == RESULT

        where:
        FILE_NAME        | RESULT
        'camelCase.xml'  | true
        'PascalCase.xml' | true
        'snake_case.xml' | true
        'kebab-case.xml' | false
    }
}