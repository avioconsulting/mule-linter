package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.GitlabFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
/**
 * This rule checks that a Gitlab file exists at the root of the project.
 */
class GitlabFileExistsRule extends Rule{

    static final String RULE_ID = 'GITLAB_EXISTS'
    static final String RULE_NAME = 'A Gitlabfile exists. '
    static final String RULE_VIOLATION_MESSAGE = 'A Gitlab file does not exist'


    GitlabFileExistsRule() {
        super(RULE_ID, RULE_NAME)
    }
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        GitlabFile gitlabFile = app.gitlabfile
        if (gitlabFile == null || !gitlabFile.doesExist()) {
            violations.add(new RuleViolation(this, app.gitlabFile.file.toString(), 0, RULE_VIOLATION_MESSAGE))
        }

        return violations
    }
}
