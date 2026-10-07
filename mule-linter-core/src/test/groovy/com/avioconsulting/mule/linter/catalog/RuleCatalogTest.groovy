package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.dsl.ConfigurationLoader
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.rule.FileExistsRule
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

class RuleCatalogTest extends Specification {
    @TempDir File directory

    def 'every built-in implementation has exactly one explicit definition'() {
        given:
        def definitions = new BuiltinRuleProvider().ruleDefinitions
        Set<String> sources = [] as Set
        new File('src/main/groovy/com/avioconsulting/mule/linter/rule').eachFileRecurse { file ->
            if (file.name.endsWith('Rule.groovy')) {
                String packageName = (file.text =~ /package ([\w.]+)/)[0][1]
                sources.add(packageName + '.' + file.name - '.groovy')
            }
        }

        expect:
        definitions.size() == 51
        definitions*.ruleClass*.name.toSet() == sources
        definitions*.id.toSet().size() == definitions.size()
        definitions.every { it.aliases == [it.reportId] }
        definitions.every { it.defaultSeverity == it.ruleClass.newInstance().severity && it.defaultType == it.ruleClass.newInstance().ruleType }
        definitions.every { definition ->
            definition.options.every { name, option -> option.description && name == option.name }
        }
    }

    def 'canonical ID and historical aliases resolve to the same definition and preserve report IDs'() {
        expect:
        RuleCatalog.instance.definitions.every { definition ->
            definition.aliases.every { RuleCatalog.instance.resolve(it).is(definition) } &&
                RuleCatalog.instance.resolve(definition.id).is(definition)
        }
        RuleCatalog.instance.resolve('logger-required-attributes').reportId == 'LOGGER_REQUIRED_ATTRIBUTES'
    }

    def 'all definitions create fresh usable instances and expose serializable metadata'() {
        expect:
        new BuiltinRuleProvider().ruleDefinitions.every { definition ->
            Map required = definition.options.findAll { name, option -> option.required && !option.hasDefault }
                .collectEntries { name, option -> [(name): example(option)] }
            def spec = definition.specification(required)
            def first = spec.instantiate()
            def second = spec.instantiate()
            def metadata = new JsonSlurper().parseText(JsonOutput.toJson(definition.describe()))
            !first.is(second) && first.ruleId == definition.reportId && metadata.id == definition.id
        }
    }

    def 'IDs and aliases occupy the same unique namespace: #id #aliases'() {
        when:
        new RuleCatalog([fixture('first', ['OLD']), fixture(id, aliases)])

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('Duplicate rule identifier')

        where:
        id       | aliases
        'first'  | []
        'second' | ['OLD']
        'second' | ['first']
        'old'    | ['first']
        'second' | ['second']
    }

    def 'canonical ID collision with an earlier alias is rejected'() {
        when:
        new RuleCatalog([fixture('first', ['second']), fixture('second', [])])
        then:
        thrown(IllegalArgumentException)
    }

    def 'typed definitions reject missing and malformed options with paths'() {
        when:
        RuleCatalog.instance.resolve(id).specification(options).instantiate()

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains(detail)

        where:
        id                           | options                                                                                         | detail
        'file-exists'                | [:]                                                                                             | 'path'
        'pom-dependency-version'     | [groupId: 'org', artifactId: 'module']                                                           | 'artifactVersion'
        'logger-required-attributes' | [requiredAttributes: [123]]                                                                     | 'requiredAttributes[0]'
        'connection-timeout-config'  | [components: [[name: 'request']]]                                                               | 'namespace'
        'connection-timeout-config'  | [components: [[name: 'request', namespace: 'uri', timeoutAtribute: 'timeout']]]                    | 'timeoutAtribute'
        'connection-timeout-config'  | [components: [[name: 'request', namespace: 5]]]                                                  | 'components[0].namespace'
        'consecutive-loggers-count'  | [excessiveLoggers: [INFO: 'two']]                                                                | 'excessiveLoggers.INFO'
        'consecutive-loggers-count'  | [excessiveLoggers: [VERBOSE: 2]]                                                                 | 'VERBOSE'
        'flow-subflow-component-count' | [maxCount: -1]                                                                               | 'at least 0'
        'global-files-no-flows'      | [patterns: ['[']]                                                                               | 'Invalid configuration'
        'on-error-log-exception'     | [exceptions: [[file: 'global.xml', handler: 'shared', errorTypes: ['ANY'], reason: 'too broad']]] | 'explicit namespace:error'
    }

    def 'nested defaults are expanded and each configured instance owns its collections'() {
        given:
        def definition = RuleCatalog.instance.resolve('connection-timeout-config')
        def specification = definition.specification(components: [[name: 'request', namespace: 'uri']])
        def first = specification.instantiate()
        def second = specification.instantiate()

        when:
        first.components[0]['config-ref'] = 'changed'

        then:
        second.components[0]['config-ref'] == 'request-config'
        second.components[0].timeoutAttribute == 'responseTimeout'
        specification.options.components[0]['config-ref'] == 'request-config'

        when:
        specification.options.components.add([:])
        then:
        thrown(UnsupportedOperationException)
    }

    def 'git-ignore configurations no longer share static options'() {
        given:
        def definition = RuleCatalog.instance.resolve('git-ignore')
        def first = definition.specification(ignoredFiles: ['first']).instantiate()
        def second = definition.specification(ignoredFiles: ['second']).instantiate()
        expect:
        first.ignoredFiles == ['first']
        second.ignoredFiles == ['second']
    }

    def 'mutable DSL collection syntax remains supported'() {
        given:
        def file = new File(directory, 'policy.groovy')
        file.text = "mule_linter { rules { LOGGER_REQUIRED_ATTRIBUTES { requiredAttributes << 'category' } } }"

        when:
        def configuration = ConfigurationLoader.load(file)

        then:
        configuration.rulesDsl.ruleSet.rules[0].requiredAttributes == ['category']
    }

    def 'collection mutation cannot bypass final specification validation'() {
        given:
        def file = new File(directory, 'policy.groovy')
        file.text = "mule_linter { rules { LOGGER_REQUIRED_ATTRIBUTES { requiredAttributes << 42 } } }"
        when:
        ConfigurationLoader.load(file)
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('requiredAttributes[0]')
    }

    def 'configuration-only loading accepts readable names and repeated rule instances'() {
        given:
        def file = new File(directory, 'policy.groovy')
        file.text = '''mule_linter { rules {
            rule('logger-required-attributes') { requiredAttributes = ['category'] }
            rule('LOGGER_REQUIRED_ATTRIBUTES') { requiredAttributes = ['message'] }
            rule('file-exists') { path = 'README.md'; severity = 'MAJOR' }
        } }'''

        when:
        def configuration = ConfigurationLoader.load(file)

        then:
        configuration.rulesDsl.specifications*.definition*.id == ['logger-required-attributes', 'logger-required-attributes', 'file-exists']
        configuration.rulesDsl.ruleSet.rules*.ruleId == ['LOGGER_REQUIRED_ATTRIBUTES', 'LOGGER_REQUIRED_ATTRIBUTES', 'FILE_EXISTS']
        configuration.rulesDsl.ruleSet.rules[2].severity == RuleSeverity.MAJOR
        directory.list().toList() == ['policy.groovy']
    }

    private static RuleDefinition fixture(String id, List<String> aliases) {
        new RuleDefinition(id, aliases, id, 'Fixture rule.', RuleSeverity.MINOR, RuleType.CODE_SMELL,
            FileExistsRule, [OptionDefinition.string('path').defaultValue('file')], { Map options -> new FileExistsRule(path: options.path) })
    }

    private static Object example(OptionDefinition option) {
        switch (option.type) {
            case 'string': return option.choices ? option.choices[0] : '1.0.0'
            case 'integer': return 1
            case 'boolean': return true
            case 'array': return []
            case 'map': case 'object': return [:]
            default: throw new IllegalArgumentException('Unhandled fixture type: ' + option.type)
        }
    }
}
