package com.avioconsulting.mule.linter.rule.git

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.GitIgnoreFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class GitIgnoreRule extends Rule {

    static final String RULE_ID = 'GIT_IGNORE'
    static final String RULE_NAME = 'A .gitignore exists and contains required expressions. '
    static final String RULE_VIOLATION_MESSAGE = 'The .gitignore is missing required expression: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'The .gitignore does not exist'
    // TODO: When using Groovy unit testing with code gen, since Studio 7 is challenged at deriving source paths from Maven, sometimes it helps to version .classpath. It might help to allow opting out of this
    static final List<String> DEFAULT_EXPRESSIONS = ['*.jar', '*.class', 'target/',
                                                     '.project', '.classpath', '.idea', 'build']
    static List<String> ignoredFiles

    GitIgnoreRule(@Param("ignoredFiles") List<String> ignoredFiles) {
        super(RULE_ID, RULE_NAME)
        this.ignoredFiles = ignoredFiles
    }

    GitIgnoreRule() {
        this(DEFAULT_EXPRESSIONS)
    }

    static GitIgnoreRule createRule(Map<String, Object> params){
        List<String> ignoredFiles = params.get("ignoredFiles") as List<String>
        if(ignoredFiles != null)
            return new GitIgnoreRule(ignoredFiles)
        else
            return new GitIgnoreRule()
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
