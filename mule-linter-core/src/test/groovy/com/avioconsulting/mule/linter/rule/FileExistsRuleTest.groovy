package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.TestApplication

import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class FileExistsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'File exists'() {
        given:
        Rule rule = new FileExistsRule()
        rule.path = "TestFile"

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        testApp.addFile(rule.path, '')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'File does not exists'() {
        given:
        Rule rule = new FileExistsRule()
        rule.path = "TestFile"
        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        testApp.removeFile(rule.path)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == 'A TestFile file does not exist'
    }
}