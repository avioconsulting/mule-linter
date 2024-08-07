package com.avioconsulting.mule.linter.rule.git

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.GitIgnoreFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.FileExistsRule

/**
 * This rule checks that a `.gitignore` file is present at root and contains the expected files and folders.
 */
class GitIgnoreRule extends FileExistsRule {

    static final String RULE_ID = 'GIT_IGNORE'
    static final String RULE_NAME = 'A .gitignore exists and contains required expressions. '
    static final String RULE_VIOLATION_MESSAGE = 'The .gitignore is missing required expression: '
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'The .gitignore does not exist'
    // TODO: When using Groovy unit testing with code gen, since Studio 7 is challenged at deriving source paths from Maven, sometimes it helps to version .classpath. It might help to allow opting out of this
    static final List<String> DEFAULT_EXPRESSIONS = ['*.jar', '*.class', 'target/',
                                                     '.project', '.classpath', '.idea', 'build']

    /**
     * ignoredFiles: is a List of the expressions to look for in the `.gitignore` file.
     * This argument is optional. By default the List is:
     * ['*.jar', '*.class', 'target/', '.project', '.classpath', '.idea', 'build']
     */
    @Param("ignoredFiles") static List<String> ignoredFiles

    GitIgnoreRule() {
        super(RULE_ID, RULE_NAME, GitIgnoreFile.GITIGNORE, FILE_MISSING_VIOLATION_MESSAGE)
        this.ignoredFiles = DEFAULT_EXPRESSIONS
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = super.execute(app)

        GitIgnoreFile gitIgnoreFile = app.gitignoreFile
        if (violations.empty) {
            ignoredFiles.each { expression ->
                if (!gitIgnoreFile.contains(expression)) {
                    violations.add(new RuleViolation(this, gitIgnoreFile.path,
                            1, RULE_VIOLATION_MESSAGE + expression))
                }
            }
        }

        return violations
    }

}
