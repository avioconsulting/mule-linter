package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.rule.FileExistsRule

/**
 * This rule checks that a Gitlab file exists at the root of the project.
 */
class GitlabFileExistsRule extends FileExistsRule{

    static final String RULE_ID = 'GITLAB_EXISTS'
    static final String RULE_NAME = 'A Gitlabfile exists. '
    public static final String GITLABFILE = '.gitlab-ci.yml'
    GitlabFileExistsRule() {
        super(RULE_ID, RULE_NAME, GITLABFILE)
    }
}
