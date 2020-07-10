package com.avioconsulting.mule.linter.rule.git

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class GitIgnoreRuleTest extends Specification {

    private final List<String> ignoredFiles = ['*.jar', '*.war', '*.ear', '*.class',
                                                    'target/', '.project', '.classpath']
    private final List<String> extraIgnoredFiles = ['*.jar', '*.war', '*.ear', '*.class',
                                                  'target/', '.project', '.classpath', '*.tmp', 'idea']
    private File appDir
    private File gitIgnore

    def setup() {
        appDir = File.createTempDir()
        gitIgnore = new File(appDir, '.gitignore')
    }

    def cleanup() {
        gitIgnore.delete()
        appDir.deleteDir()
    }

    def 'Correct expressions in .gitignore'() {
        given:
        gitIgnore.withPrintWriter { pw ->
            ignoredFiles.each {
                pw.println(it)
            }
        }
        Application app = new Application(appDir)

        when:
        Rule rule = new GitIgnoreRule(ignoredFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Missing expressions in .gitignore'() {
        given:
        gitIgnore.withPrintWriter { pw ->
            ignoredFiles.each {
                pw.println(it)
            }
        }
        Application app = new Application(appDir)

        when:
        Rule rule = new GitIgnoreRule(extraIgnoredFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].lineNumber == 1
        violations[1].lineNumber == 1
        violations[0].message.contains('*.tmp')
        violations[1].message.contains('idea')
    }

    def 'Missing .gitignore file'() {
        given:
        Application app = new Application(appDir)
        gitIgnore.delete()

        when:
        Rule rule = new GitIgnoreRule(ignoredFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 0
        violations[0].message == GitIgnoreRule.FILE_MISSING_VIOLATION_MESSAGE
    }

    def 'Correct expressions with blank lines and comments'() {
        given:
        gitIgnore.withPrintWriter { pw ->
            pw.println('# Beginning Comment')
            pw.println('')
            ignoredFiles[0..2].each {
                pw.println(it)
            }
            pw.println('')
            pw.println('# Middle Comment')
            ignoredFiles[3..-1].each {
                pw.println(it)
            }
            pw.println('# Ending Comment')
        }
        Application app = new Application(appDir)

        when:
        Rule rule = new GitIgnoreRule(ignoredFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
        !app.gitignoreFile.contains('')
        !app.gitignoreFile.contains('# Ending Comment')
    }

}
