package com.avioconsulting.mule.linter.rule.git

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.GitIgnoreFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class GitIgnoreRule extends Rule {

    static final String RULE_ID = 'GIT_IGNORE'
    static final String RULE_NAME = 'A .gitignore exists and contains required expressions. '
    static final String RULE_VIOLATION_MESSAGE = 'The .gitignore is missing required expression: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'The .gitignore does not exist'
    static final List<String> DEFAULT_EXPRESSIONS = ['*.jar', '*.class', 'target/',
                                                     '.project', '.classpath', '.idea', 'build']
    static List<String> ignoredFiles

    GitIgnoreRule(List<String> ignoredFiles) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.ignoredFiles = ignoredFiles
    }

    GitIgnoreRule() {
        this(DEFAULT_EXPRESSIONS)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        GitIgnoreFile gitIgnoreFile = app.gitignoreFile
        if (gitIgnoreFile.doesExist()) {
            ignoredFiles.each { expression ->
                if (!gitIgnoreFile.contains(expression)) {
                    violations.add(new RuleViolation(this, gitIgnoreFile.path,
                            1, RULE_VIOLATION_MESSAGE + expression))
                }
            }
        } else {
            violations.add(new RuleViolation(this, gitIgnoreFile.path,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }

        return violations
    }

}
