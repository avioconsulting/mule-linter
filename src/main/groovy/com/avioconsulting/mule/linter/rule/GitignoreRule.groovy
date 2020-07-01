package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.GitIgnoreFile
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class GitignoreRule extends Rule{

    static final String RULE_ID = 'POM_EXISTS'
    static final String RULE_NAME = 'pom.xml file exists'
    static final String RULE_VIOLATION_MESSAGE = 'missing untracked files: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'File does not exist.'
    static String[] untrackedFiles

    GitignoreRule(String[] untrackedFiles) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.untrackedFiles = untrackedFiles
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        GitIgnoreFile gitIgnoreFile = app.gitignoreFile
        if (gitIgnoreFile.doesExist()) {
            String[] notFound = gitIgnoreFile.checkUntrackedFiles(untrackedFiles)
            if (notFound.size() > 0) {
                violations.add(new RuleViolation(this, app.gitignoreFile.path,
                        1, RULE_VIOLATION_MESSAGE + notFound))
            }
        } else {
            violations.add(new RuleViolation(this, app.gitignoreFile.path,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }

        return violations
    }
}
