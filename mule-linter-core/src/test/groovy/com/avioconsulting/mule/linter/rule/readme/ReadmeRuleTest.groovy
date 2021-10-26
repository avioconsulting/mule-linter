package com.avioconsulting.mule.linter.rule.readme

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ReadmeFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ReadmeRuleTest extends Specification {
    private File appDir
    private File readme

    private static final String README_CONTENTS = '''# Example README file

This is an example readme to test that readme's are checked

## Inside a test

I would expect to see this file in a test about the README.md
'''

    def setup() {
        appDir = File.createTempDir()
        readme = new File(appDir, ReadmeFile.README)
    }

    def cleanup() {
        readme.delete()
        appDir.deleteDir()
    }

    def 'README.md has content'() {
        given:
        readme.withPrintWriter { pw ->
            pw.print(README_CONTENTS)
        }
        Application app = new Application(appDir)

        when:
        Rule rule = new ReadmeRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'README.md has no content'() {
        given:
        readme.withPrintWriter { pw ->
            pw.print('')
        }
        Application app = new Application(appDir)

        when:
        Rule rule = new ReadmeRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == ReadmeRule.RULE_VIOLATION_MESSAGE
    }

    def 'Missing README.md file'() {
        given:
        readme.delete()
        Application app = new Application(appDir)

        when:
        Rule rule = new ReadmeRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == ReadmeRule.FILE_MISSING_VIOLATION_MESSAGE
    }
}
