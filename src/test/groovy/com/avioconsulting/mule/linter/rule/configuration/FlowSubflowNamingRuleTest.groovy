package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class FlowSubflowNamingRuleTest extends Specification {

    private static final String NAMING_APP = 'FlowSubflowNaming'

    private static final String CAMEL_CASE_FILE = 'camelCase.xml'
    private static final String CAMEL_CASE_INCORRECT_FILE = 'camelCase-incorrect.xml'
    private static final String PASCAL_CASE_FILE = 'PascalCase.xml'
    private static final String PASCAL_CASE_INCORRECT_FILE = 'PascalCase-incorrect.xml'
    private static final String SNAKE_CASE_FILE = 'snake_case.xml'
    private static final String SNAKE_CASE_INCORRECT_FILE = 'snake_case-incorrect.xml'
    private static final String KEBAB_CASE_FILE = 'kebab-case.xml'
    private static final String KEBAB_CASE_INCORRECT_FILE = 'kebab-case-incorrect.xml'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'cameCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.CAMEL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        !violations.any { it.fileName.contains(CAMEL_CASE_FILE) }

        violations.any { it.fileName.contains(PASCAL_CASE_FILE) }
        violations.count { it.fileName.contains(PASCAL_CASE_FILE) } == 4

        violations.any { it.fileName.contains(SNAKE_CASE_FILE) }
        violations.count { it.fileName.contains(SNAKE_CASE_FILE) } == 2

        violations.any { it.fileName.contains(KEBAB_CASE_FILE) }
        violations.count { it.fileName.contains(KEBAB_CASE_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect cameCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.CAMEL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(CAMEL_CASE_INCORRECT_FILE) }
        violations.count { it.fileName.contains(CAMEL_CASE_INCORRECT_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'PascalCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.PASCAL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(CAMEL_CASE_FILE) }
        violations.count { it.fileName.contains(CAMEL_CASE_FILE) } == 2

        !violations.any { it.fileName.contains(PASCAL_CASE_FILE) }

        violations.any { it.fileName.contains(SNAKE_CASE_FILE) }
        violations.count { it.fileName.contains(SNAKE_CASE_FILE) } == 2

        violations.any { it.fileName.contains(KEBAB_CASE_FILE) }
        violations.count { it.fileName.contains(KEBAB_CASE_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect PascalCase flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.PASCAL_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(PASCAL_CASE_INCORRECT_FILE) }
        violations.count { it.fileName.contains(PASCAL_CASE_INCORRECT_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'snake_case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.SNAKE_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(CAMEL_CASE_FILE) }
        violations.count { it.fileName.contains(CAMEL_CASE_FILE) } == 2

        violations.any { it.fileName.contains(PASCAL_CASE_FILE) }
        violations.count { it.fileName.contains(PASCAL_CASE_FILE) } == 4

        !violations.any { it.fileName.contains(SNAKE_CASE_FILE) }

        violations.any { it.fileName.contains(KEBAB_CASE_FILE) }
        violations.count { it.fileName.contains(KEBAB_CASE_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect snake_case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.SNAKE_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(SNAKE_CASE_INCORRECT_FILE) }
        violations.count { it.fileName.contains(SNAKE_CASE_INCORRECT_FILE) } == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'kebab-case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.KEBAB_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(CAMEL_CASE_FILE) }
        violations.count { it.fileName.contains(CAMEL_CASE_FILE) } == 2

        violations.any { it.fileName.contains(PASCAL_CASE_FILE) }
        violations.count { it.fileName.contains(PASCAL_CASE_FILE) } == 4

        violations.any { it.fileName.contains(SNAKE_CASE_FILE) }
        violations.count { it.fileName.contains(SNAKE_CASE_FILE) } == 2

        !violations.any { it.fileName.contains(KEBAB_CASE_FILE) }
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect kebab-case flow subflow naming convention check'() {
        given:
        Rule rule = new FlowSubflowNamingRule(FlowSubflowNamingRule.CaseFormat.KEBAB_CASE)

        when:
        File appDir = new File(this.class.classLoader.getResource(NAMING_APP).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.any { it.fileName.contains(KEBAB_CASE_INCORRECT_FILE) }
        violations.count { it.fileName.contains(KEBAB_CASE_INCORRECT_FILE) } == 2
    }
}