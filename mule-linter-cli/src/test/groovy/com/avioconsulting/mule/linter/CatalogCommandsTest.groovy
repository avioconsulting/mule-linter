package com.avioconsulting.mule.linter

import groovy.json.JsonSlurper
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir

class CatalogCommandsTest extends Specification {
    @TempDir File directory
    StringWriter output = new StringWriter()
    StringWriter errors = new StringWriter()

    def 'list operates without application or config inputs and supports JSON'() {
        expect:
        execute('rules', 'list', '--json') == 0
        def data = json()
        data.schemaVersion == 1
        data.rules.size() == 51
        data.rules*.id == data.rules*.id.sort(false)
        data.rules.find { it.id == 'logger-required-attributes' }.aliases == ['LOGGER_REQUIRED_ATTRIBUTES']
        errors.toString().empty
    }

    def 'human list and description are readable'() {
        expect:
        execute('rules', 'list') == 0
        output.toString().contains('logger-required-attributes [CRITICAL]')

        when:
        output.buffer.setLength(0)
        then:
        execute('rules', 'describe', 'logger-required-attributes') == 0
        output.toString().contains('Aliases: LOGGER_REQUIRED_ATTRIBUTES')
        output.toString().contains('requiredAttributes')
    }

    def 'describe canonical ID and alias return identical contracts'() {
        given:
        assert execute('rules', 'describe', 'logger-required-attributes', '--json') == 0
        def canonical = json()
        output.buffer.setLength(0)

        expect:
        execute('rules', 'describe', 'LOGGER_REQUIRED_ATTRIBUTES', '--json') == 0
        json() == canonical
        canonical.rule.options.requiredAttributes.items.type == 'string'
        canonical.rule.options.requiredAttributes.defaultValue == []
    }

    def 'validate config performs structural and domain validation without application loading'() {
        given:
        def config = new File(directory, 'policy.groovy')
        config.text = text

        expect:
        execute('config', 'validate', config.path, '--json') == status
        json().valid == valid
        if (valid) {
            assert json().ruleCount == 2
            assert json().rules*.id == ['file-exists', 'file-exists']
        } else assert json().errors[0].contains(detail)
        directory.list().toList() == ['policy.groovy']

        where:
        text                                                                                       | status | valid | detail
        "mule_linter { rules { rule('file-exists') { path = 'one' }; FILE_EXISTS { path = 'two' } } }" | 0      | true  | ''
        'mule_linter { rules { UNKNOWN_RULE } }'                                                     | 2      | false | 'Unknown rule'
        'mule_linter { rules { FILE_EXISTS {} } }'                                                  | 2      | false | 'path'
        "mule_linter { rules { LOGGER_REQUIRED_ATTRIBUTES { requiredAttributes = [42] } } }"       | 2      | false | 'requiredAttributes[0]'
        "mule_linter { rules { GLOBAL_FILES_NO_FLOWS { patterns = ['['] } } }"                      | 2      | false | 'Invalid configuration'
        'invalid {'                                                                                | 2      | false | 'startup failed'
        '42'                                                                                       | 2      | false | 'must return'
    }

    def 'script println diagnostics do not contaminate machine readable output'() {
        given:
        def config = new File(directory, 'policy.groovy')
        config.text = "println 'diagnostic'; mule_linter { rules { README } }"
        expect:
        execute('config', 'validate', config.path, '--json') == 0
        json().valid
        errors.toString().contains('diagnostic')
    }

    def 'unknown rule and missing file return structured errors'() {
        expect:
        execute(*arguments as String[]) == 2
        json().valid == false
        json().errors.size() == 1
        where:
        arguments << [['rules', 'describe', 'unknown', '--json'], ['config', 'validate', '/not/a/config.groovy', '--json']]
    }

    def 'groups and subcommands retain help and reject missing required arguments'() {
        expect:
        execute(*arguments as String[]) == status
        where:
        arguments                 | status
        ['--help']                | 0
        ['rules', '--help']       | 0
        ['config', '--help']      | 0
        ['rules', 'describe']     | 2
        ['config', 'validate']    | 2
        ['rules']                 | 2
    }

    private int execute(String... arguments) {
        def command = new CommandLine(new MuleLinterCli())
        command.out = new PrintWriter(output)
        command.err = new PrintWriter(errors)
        command.execute(arguments)
    }

    private Map json() { new JsonSlurper().parseText(output.toString()) as Map }
}
