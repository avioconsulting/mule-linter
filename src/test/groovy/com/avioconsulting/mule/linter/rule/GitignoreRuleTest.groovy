package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.rule.GitignoreRule
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class GitignoreRuleTest extends Specification {

    private static final String APP_NAME = 'SampleMuleApp'
    private static final String APP_NAME_BAD = 'BadMuleApp'
    private File fileOutput
    private final String[] correctUntrackedFiles = ['*.jar', '*.war', '*.ear', '*.class',
                                                    'target/', '.project', '.classpath']
    private final String[] extraUntrackedFiles = ['*.jar', '*.war', '*.ear', '*.class',
                                                  'target/', '.project', '.classpath','*.tmp']

    def setup() {

        Path path = Paths.get(this.class.classLoader.getResource(APP_NAME).path + '/.gitignore')
        path.resolve('.gitignore')
        fileOutput = new File(path.toString())
        if (fileOutput.exists()) {
            fileOutput.delete()
        }

        fileOutput.createNewFile()

        FileOutputStream fos = new FileOutputStream(fileOutput);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        correctUntrackedFiles.each {
            file ->
            bw.write(file)
            bw.newLine()
        }
        bw.close();
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Correct Gitignore untracked files'() {
        given:
        Application app = loadApp(APP_NAME)

        when:
        Rule rule = new GitignoreRule(correctUntrackedFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0

    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect Gitignore untracked files'() {
        given:
        Application app = loadApp(APP_NAME)

        when:
        Rule rule = new GitignoreRule(extraUntrackedFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1

    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing Gitignore file'() {
        given:
        Application app = loadApp(APP_NAME_BAD)

        when:
        Rule rule = new GitignoreRule(correctUntrackedFiles)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    private Application loadApp(String appName) {
        File appDir = new File(this.class.classLoader.getResource(appName).file)
        new Application(appDir)
    }
}