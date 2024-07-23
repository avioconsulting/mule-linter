package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.rule.FileExistsRule

/**
 * This rule checks that a Jenkinsfile exists at the root of the project.
 */
class JenkinsFileExistsRule extends FileExistsRule {

    static final String RULE_ID = 'JENKINS_EXISTS'
    static final String RULE_NAME = 'A Jenkinsfile exists. '
    public static final String JENKINSFILE = 'Jenkinsfile'
    JenkinsFileExistsRule() {
        super(RULE_ID, RULE_NAME, JENKINSFILE)
    }

}
