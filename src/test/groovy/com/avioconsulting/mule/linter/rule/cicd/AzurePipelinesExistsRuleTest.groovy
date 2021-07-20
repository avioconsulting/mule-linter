package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.AzurePipelinesFile
import com.avioconsulting.mule.linter.model.JenkinsFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class AzurePipelinesExistsRuleTest extends Specification {
    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.addAzurePipelinesFile()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'azure pipelines exists'() {
        given:
        Rule rule = new AzurePipelinesExistsRule()
        Application app = new Application(testApp.appDir)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'azure pipelines does not exist'() {
        given:
        Rule rule = new AzurePipelinesExistsRule()
        Application app = new Application(testApp.appDir)
        testApp.removeFile(AzurePipelinesFile.AZURE_PIPELINES)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == AzurePipelinesExistsRule.RULE_VIOLATION_MESSAGE
    }

    def 'azure pipelines is not valid YAML'() {
        given:
        Rule rule = new AzurePipelinesExistsRule()
        Application app = new Application(testApp.appDir)
        testApp.removeFile(AzurePipelinesFile.AZURE_PIPELINES)
        testApp.addAzurePipelinesFile(false)

        when:
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == AzurePipelinesExistsRule.RULE_VIOLATION_MESSAGE
    }
}
