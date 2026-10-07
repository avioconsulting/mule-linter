package com.avioconsulting.mule.linter

import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir

class MuleLinterCliTest extends Specification {
    @TempDir File directory
    File config
    PrintStream originalOut
    ByteArrayOutputStream output

    def setup() {
        config = new File(directory, 'rules.groovy')
        originalOut = System.out
        output = new ByteArrayOutputStream()
        System.setOut(new PrintStream(output))
    }

    def cleanup() { System.setOut(originalOut) }

    def 'CLI report only default and enforcement matrix: #severity #options'() {
        given:
        config.text = "mule_linter { rules { FILE_EXISTS { path = 'missing'; severity = '$severity' } } }"
        expect:
        execute(options) == status
        output.toString().contains('1 violations')
        where:
        severity   | options                           | status
        'BLOCKER'  | []                                | 0
        'CRITICAL' | []                                | 0
        'MAJOR'    | []                                | 0
        'MINOR'    | []                                | 0
        'BLOCKER'  | ['--fail']                        | 1
        'CRITICAL' | ['--fail']                        | 1
        'MAJOR'    | ['--fail']                        | 1
        'MINOR'    | ['--fail']                        | 0
        'MINOR'    | ['--fail', '--threshold', 'MINOR'] | 1
        'MAJOR'    | ['--fail', '--threshold', 'BLOCKER'] | 0
        'BLOCKER'  | ['--threshold', 'MINOR']           | 0
    }

    def 'successful completed analysis returns zero and preserves existing flags'() {
        given:
        config.text = "mule_linter { rules { FILE_EXISTS { path = 'rules.groovy' } } }"
        expect:
        execute(['--fail', '-f', 'JSON']) == 0
        output.toString().contains('"issues": []')
    }

    def 'invalid syntax and configuration fail even in report only mode'() {
        given:
        config.text = rules
        expect:
        execute(options) == 2
        where:
        rules                                                             | options
        'mule_linter { rules { UNKNOWN_RULE } }'                           | []
        'mule_linter { rules { FILE_EXISTS { paht = "missing" } } }'       | []
        'invalid {'                                                       | []
        'mule_linter { rules { README } }'                                 | ['--threshold', 'INVALID']
        'mule_linter { rules { README } }'                                 | ['--unknown']
        'mule_linter { rules { README } }'                                 | ['-f', 'INVALID']
    }

    def 'missing inputs and missing application are operational errors'() {
        given:
        config.text = 'mule_linter { rules { README } }'
        expect:
        command().execute() == 2
        command().execute('-r', config.path, '-d', new File(directory, 'absent').path) == 2
        command().execute('-r', new File(directory, 'absent.groovy').path, '-d', directory.path) == 2
    }

    def 'rule execution errors always return two'() {
        given:
        config.text = '''
            def configuration = mule_linter { rules { README } }
            configuration.rulesDsl.ruleSet.rules[0].metaClass.execute = { com.avioconsulting.mule.linter.model.Application app -> throw new IllegalStateException('rule failed') }
            configuration
        '''
        expect:
        execute([]) == 2
    }

    def 'report write error always returns two even report only'() {
        given:
        config.text = 'mule_linter { rules { README } }'
        System.setOut(new PrintStream(new OutputStream() {
            void write(int value) { throw new IOException('write failed') }
        }))
        expect:
        execute([]) == 2
    }

    def 'incomplete analysis warns by default and optional strict mode returns two'() {
        given:
        config.text = '''
            def configuration = mule_linter { rules { README } }
            configuration.rulesDsl.ruleSet.rules[0].metaClass.execute = { com.avioconsulting.mule.linter.model.Application app ->
                app.analysisWarnings.add('Unresolved parent fixture')
                []
            }
            configuration
        '''
        expect:
        execute(options) == status
        output.toString().contains('Analysis incomplete.')
        where:
        options      | status
        []           | 0
        ['--fail']   | 0
        ['--strict'] | 2
    }

    private int execute(List<String> options) {
        command().execute((['-r', config.path, '-d', directory.path] + options) as String[])
    }

    private CommandLine command() {
        def command = new CommandLine(new MuleLinterCli())
        command.err = new PrintWriter(new StringWriter())
        command
    }
}
