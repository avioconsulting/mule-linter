package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.MuleLinter
import com.avioconsulting.mule.linter.model.ReportFormat
import spock.lang.Specification
import spock.lang.TempDir

class YamlConfigurationLoaderTest extends Specification {
    @TempDir File directory

    def 'YAML and Groovy produce equivalent specifications and findings'() {
        given:
        def yaml = write('policy.yaml', '''schemaVersion: 1
rules:
  - rule: file-exists
    options:
      path: missing
      severity: MAJOR
  - rule: FILE_EXISTS
    options:
      path: policy.yaml
  - rule: logger-required-attributes
    options:
      requiredAttributes: [category, message]
''')
        def groovy = write('policy.groovy', '''mule_linter { rules {
            rule('file-exists') { path = 'missing'; severity = 'MAJOR' }
            FILE_EXISTS { path = 'policy.yaml' }
            LOGGER_REQUIRED_ATTRIBUTES { requiredAttributes = ['category', 'message'] }
        } }''')

        when:
        def left = ConfigurationLoader.load(yaml)
        def right = ConfigurationLoader.load(groovy)
        def yamlResult = new MuleLinter(directory, yaml, ReportFormat.JSON).buildLinterExecutor().analysisResult
        def groovyResult = new MuleLinter(directory, groovy, ReportFormat.JSON).buildLinterExecutor().analysisResult

        then:
        left.rulesDsl.specifications*.definition*.id == right.rulesDsl.specifications*.definition*.id
        left.rulesDsl.specifications*.options == right.rulesDsl.specifications*.options
        yamlResult.findings == groovyResult.findings
        yamlResult.ruleCount == 3
    }

    def 'yaml suffixes and case are recognized without executing Groovy'() {
        expect:
        ConfigurationLoader.load(write(name, 'schemaVersion: 1\nrules: []')).rulesDsl.specifications.empty
        where:
        name << ['policy.yaml', 'policy.yml', 'policy.YAML', 'policy.YML']
    }

    def 'nested defaults and enum options are applied through the catalog'() {
        when:
        def configuration = ConfigurationLoader.load(write('policy.yml', '''schemaVersion: 1
rules:
  - rule: connection-timeout-config
    options:
      components:
        - name: request
          namespace: uri
      severity: BLOCKER
'''))
        then:
        def rule = configuration.rulesDsl.ruleSet.rules[0]
        rule.components == [[name: 'request', namespace: 'uri', 'config-ref': 'request-config', timeoutAttribute: 'responseTimeout']]
        rule.severity.name() == 'BLOCKER'
    }

    def 'invalid YAML structure or options fail with useful context: #detail'() {
        when:
        ConfigurationLoader.load(write('policy.yaml', text))
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains(detail)
        where:
        text                                                                                                                     | detail
        ''                                                                                                                       | 'mapping'
        '[]'                                                                                                                     | 'mapping'
        'schemaVersion: 2\nrules: []'                                                                                             | 'schemaVersion: 1'
        'schemaVersion: "1"\nrules: []'                                                                                          | 'schemaVersion: 1'
        'rules: []'                                                                                                              | 'schemaVersion: 1'
        'schemaVersion: 1'                                                                                                       | 'rules list'
        'schemaVersion: 1\nrules: null'                                                                                           | 'rules list'
        'schemaVersion: 1\nrules: {}'                                                                                             | 'rules list'
        'schemaVersion: 1\nrules: []\nrulez: []'                                                                                   | 'rulez'
        'schemaVersion: 1\nrules: [readme]'                                                                                       | 'rules[0] must be a mapping'
        'schemaVersion: 1\nrules: [{}]'                                                                                           | 'rule identifier'
        'schemaVersion: 1\nrules: [{rule: 123}]'                                                                                   | 'rule identifier'
        'schemaVersion: 1\nrules: [{rule: readme, options: null}]'                                                                 | 'rules[0].options'
        'schemaVersion: 1\nrules: [{rule: readme, severity: MAJOR}]'                                                               | 'severity'
        'schemaVersion: 1\nrules: [{rule: typo}]'                                                                                  | 'rules[0]: Unknown rule'
        'schemaVersion: 1\nrules: [{rule: file-exists}]'                                                                           | 'required option'
        'schemaVersion: 1\nrules: [{rule: readme, options: {paht: file}}]'                                                         | 'paht'
        'schemaVersion: 1\nrules: [{rule: logger-required-attributes, options: {requiredAttributes: [123]}}]'                       | 'requiredAttributes[0]'
        'schemaVersion: 1\nrules: [{rule: connection-timeout-config, options: {components: [{name: request}]}}]'                    | 'namespace'
        'schemaVersion: 1\nrules: [{rule: global-files-no-flows, options: {patterns: ["["]}}]'                                     | 'Invalid configuration'
        'schemaVersion: 1\nrules: [{rule: readme, options: {severity: MJAOR}}]'                                                    | 'expected one of'
        'schemaVersion: 1\nrules: [{rule: flow-subflow-component-count, options: {maxCount: "20"}}]'                              | 'expected Integer'
    }

    def 'unsafe tags duplicate keys aliases and multiple documents are rejected'() {
        when:
        ConfigurationLoader.load(write('policy.yaml', text))
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('Invalid YAML')
        where:
        text << [
            '!!java.lang.ProcessBuilder {}',
            'schemaVersion: 1\nschemaVersion: 1\nrules: []',
            'schemaVersion: 1\nrules: [{rule: readme, rule: readme}]',
            'schemaVersion: 1\nrules: [{rule: file-exists, options: {path: one, path: two}}]',
            'schemaVersion: 1\nrules: &rules [{rule: readme}, *rules]',
            'schemaVersion: 1\nrules: []\n---\nschemaVersion: 1\nrules: []'
        ]
    }

    def 'parser limits reject excessively deep and large documents'() {
        when:
        ConfigurationLoader.load(write('policy.yaml', text))
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('Invalid YAML')
        where:
        text << ['[' * 60 + '0' + ']' * 60, 'text: ' + 'x' * (1024 * 1024 + 1)]
    }

    def 'invalid YAML fails before application loading'() {
        when:
        new MuleLinter(new File(directory, 'absent'), write('policy.yaml', 'schemaVersion: 1\nrules: [{rule: unknown}]'), ReportFormat.JSON)
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('Unknown rule identifier')
    }

    def 'unknown suffix is not evaluated as Groovy'() {
        given:
        def marker = new File(directory, 'executed')
        def file = write('policy.txt', "new File('${marker.path}').text = 'executed'")
        when:
        ConfigurationLoader.load(file)
        then:
        thrown(IllegalArgumentException)
        !marker.exists()
    }

    def 'checked-in YAML example validates without an application'() {
        expect:
        ConfigurationLoader.load(new File('mule-linter-example.yaml')).rulesDsl.specifications.size() == 5
    }

    private File write(String name, String text) {
        def file = new File(directory, name)
        file.setText(text, 'UTF-8')
        file
    }
}
