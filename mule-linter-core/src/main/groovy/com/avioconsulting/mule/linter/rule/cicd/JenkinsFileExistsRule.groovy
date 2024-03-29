package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.JenkinsFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
/**
 * This rule checks that a Jenkinsfile exists at the root of the project.
 */
class JenkinsFileExistsRule extends Rule{

    static final String RULE_ID = 'JENKINS_EXISTS'
    static final String RULE_NAME = 'A Jenkinsfile exists. '
    static final String RULE_VIOLATION_MESSAGE = 'A Jenkinsfile file does not exist'


    JenkinsFileExistsRule() {
        super(RULE_ID, RULE_NAME)
    }
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        JenkinsFile jenkinsFile = app.jenkinsfile
        if (jenkinsFile == null || !jenkinsFile.doesExist()) {
            violations.add(new RuleViolation(this, app.jenkinsFile.file.toString(), 0, RULE_VIOLATION_MESSAGE))
        }

        return violations
    }
}
