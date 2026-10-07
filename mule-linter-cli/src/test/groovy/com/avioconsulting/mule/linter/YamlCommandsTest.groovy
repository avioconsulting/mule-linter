package com.avioconsulting.mule.linter

import groovy.json.JsonSlurper
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir

class YamlCommandsTest extends Specification {
    @TempDir File directory
    StringWriter output = new StringWriter()
    StringWriter errors = new StringWriter()

    def 'config validate supports YAML with machine-readable success and errors'() {
        given:
        def yaml = new File(directory, 'policy.yml')
        yaml.text = text
        when:
        int status = command().execute('config', 'validate', yaml.path, '--json')
        def result = new JsonSlurper().parseText(output.toString())
        then:
        status == expectedStatus
        result.valid == valid
        result.schemaVersion == 1
        if (valid) assert result.rules*.reportId == ['README']
        else assert result.errors[0].contains('required option')
        where:
        text                                      | expectedStatus | valid
        'schemaVersion: 1\nrules: [{rule: readme}]' | 0              | true
        'schemaVersion: 1\nrules: [{rule: file-exists}]' | 2          | false
    }

    def 'analysis accepts YAML and preserves enforcement and report identity'() {
        given:
        def yaml = new File(directory, 'policy.yaml')
        yaml.text = 'schemaVersion: 1\nrules: [{rule: file-exists, options: {path: missing, severity: MAJOR}}]'
        def original = System.out
        def report = new ByteArrayOutputStream()
        System.setOut(new PrintStream(report))
        expect:
        command().execute((['-r', yaml.path, '-d', directory.path, '-f', 'JSON'] + flags) as String[]) == status
        new JsonSlurper().parseText(report.toString()).issues[0].ruleId == 'FILE_EXISTS'
        cleanup:
        System.setOut(original)
        where:
        flags                             | status
        []                                | 0
        ['--fail']                        | 1
        ['--fail', '--threshold', 'BLOCKER'] | 0
    }

    def 'config schema emits raw draft 2020-12 JSON for IDEs'() {
        when:
        int status = command().execute('config', 'schema')
        def schema = new JsonSlurper().parseText(output.toString())
        then:
        status == 0
        schema.'$schema' == 'https://json-schema.org/draft/2020-12/schema'
        schema.'$defs'.size() == 51
        !schema.containsKey('schemaVersion')
        errors.toString().empty
    }

    def 'config schema writes UTF-8 files instead of stdout and can regenerate them'() {
        given:
        def schemaFile = new File(directory, 'mule-linter.schema.json')
        schemaFile.text = 'old schema'
        expect:
        command().execute('config', 'schema', '-o', schemaFile.path) == 0
        new JsonSlurper().parse(schemaFile, 'UTF-8').'$defs'.size() == 51
        output.toString().empty
    }

    def 'schema output failures return two'() {
        expect:
        command().execute('config', 'schema', '--output', directory.path) == 2
        errors.toString().contains('Linter error')
    }

    def 'schema stdout write failures return two'() {
        given:
        def cli = command()
        cli.out = new PrintWriter(new Writer() {
            void write(char[] value, int offset, int count) { throw new IOException('write failed') }
            void flush() { }
            void close() { }
        })
        expect:
        cli.execute('config', 'schema') == 2
        errors.toString().contains('Failed to write schema output')
    }

    private CommandLine command() {
        def cli = new CommandLine(new MuleLinterCli())
        cli.out = new PrintWriter(output)
        cli.err = new PrintWriter(errors)
        cli
    }
}
