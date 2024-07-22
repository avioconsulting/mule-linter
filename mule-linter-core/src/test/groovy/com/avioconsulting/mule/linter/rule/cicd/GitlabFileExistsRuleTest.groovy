package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.TestApplication

import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class GitlabFileExistsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addGitlabFile()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Gitlab file exists'() {
        given:
        Rule rule = new GitlabFileExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Gitlab file does not exists'() {
        given:
        Rule rule = new GitlabFileExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        testApp.removeFile(GitlabFileExistsRule.GITLABFILE)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == "A .gitlab-ci.yml file does not exist"
    }
}