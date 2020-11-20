package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.JenkinsFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class JenkinsFileExistsRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addJenkinsfile()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Jenkinsfile exists'() {
        given:
        Rule rule = new JenkinsFileExistsRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Jenkinsfile does not exists'() {
        given:
        Rule rule = new JenkinsFileExistsRule()

        when:
        Application app = new Application(testApp.appDir)
        testApp.removeFile(JenkinsFile.JENKINSFILE)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == JenkinsFileExistsRule.RULE_VIOLATION_MESSAGE
    }
}