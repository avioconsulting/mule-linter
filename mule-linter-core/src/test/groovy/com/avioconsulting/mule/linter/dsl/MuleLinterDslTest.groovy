package com.avioconsulting.mule.linter.dsl

import org.codehaus.groovy.control.CompilerConfiguration
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import spock.lang.Specification

class MuleLinterDslTest extends Specification {

    def "Test Rule DSL"() {
        given:
        def dsl = this.class.getClassLoader().getResource("TestRules.groovy").path

        when:
        def compilerConfig = new CompilerConfiguration().with {
            scriptBaseClass = Dsl.name
            it
        }
        def binding = new Binding()
        binding.setVariable('params',[:])

        def shell = new GroovyShell(
                this.class.classLoader,
                binding,
                compilerConfig
        )
        MuleLinterDsl ruleConfig = shell.evaluate(new File(dsl)) as MuleLinterDsl
        then:
        def rules = ruleConfig.rulesDsl.ruleSet.rules
        rules*.ruleId == [
                'AZURE_PIPELINES_EXISTS', 'JENKINS_EXISTS', 'GITLAB_EXISTS', 'FILE_EXISTS',
                'API_CONSOLE_DISABLED', 'COMMENTED_CODE', 'COMPONENT_REQUIRED_ATTRIBUTES',
                'COMPONENT_COUNT', 'CONFIG_FILE_NAMING', 'CONFIG_PLACEHOLDER', 'COMPONENT_DISPLAY_NAME',
                'CONSECUTIVE_LOGGERS_COUNT', 'CONSECUTIVE_LOGGERS_COUNT', 'FLOW_SUBFLOW_NAMING',
                'GLOBAL_CONFIG_NO_FLOWS', 'GLOBAL_CONFIG_NO_FLOWS', 'GLOBAL_CONFIG_EXISTS',
                'LOGGER_REQUIRED_ATTRIBUTES', 'LOGGER_CATEGORY_HASVALUE', 'LOGGER_MESSAGE_CONTENTS',
                'LOGGER_MESSAGE_HASVALUE', 'MULE_CONFIG_FLOW_LIMIT', 'ON_ERROR_LOG_EXCEPTION',
                'UNTIL_SUCCESSFUL', 'UNUSED_FLOW', 'GIT_IGNORE', 'GIT_IGNORE',
                'MULE_ARTIFACT_SECURE_PROPERTIES', 'MULE_ARTIFACT_MIN_MULE_VERSION', 'MULE_MAVEN_PLUGIN',
                'MULE_RUNTIME', 'MUNIT_MAVEN_PLUGIN_ATTRIBUTES', 'MUNIT_PLUGIN_VERSION', 'MUNIT_VERSION',
                'POM_DEPENDENCY_VERSION', 'POM_FILE_EXISTS', 'POM_PLUGIN_ATTRIBUTE', 'MAVEN_PROPERTY',
                'ENCRYPTED_VALUE', 'HOSTNAME_PROPERTY', 'PROPERTY_EXISTS', 'PROPERTY_FILE_NAMING',
                'PROPERTY_FILE_COUNT_MISMATCH', 'README'
        ]
        rules.find { it.ruleId == 'FILE_EXISTS' }.path == 'sample.json'
        rules.find { it.ruleId == 'LOGGER_REQUIRED_ATTRIBUTES' }.requiredAttributes == ['category']
        rules.find { it.ruleId == 'COMPONENT_REQUIRED_ATTRIBUTES' }.requiredAttributes == ['name']
        rules.findAll { it.ruleId == 'CONSECUTIVE_LOGGERS_COUNT' }*.excessiveLoggers == [[INFO: 3, DEBUG: 2], 2]
        !rules[11].is(rules[12])
        rules.find { it.ruleId == 'MULE_CONFIG_FLOW_LIMIT' }.flowLimit == 2


    }

    def "metadata, interpolation, setters and repeated instances remain supported"() {
        when:
        def rules = evaluate('''
            FILE_EXISTS {
                path = "${'first'}.xml"
                severity = 'MAJOR'
                ruleType = 'BUG'
                setRuleName('Custom name')
            }
            FILE_EXISTS { path = 'second.xml' }
            README
            POM_FILE_EXISTS()
        ''').rulesDsl.ruleSet.rules

        then:
        rules*.ruleId == ['FILE_EXISTS', 'FILE_EXISTS', 'README', 'POM_FILE_EXISTS']
        rules[0].path == 'first.xml'
        rules[0].severity == RuleSeverity.MAJOR
        rules[0].ruleType == RuleType.BUG
        rules[0].ruleName == 'Custom name'
        rules[1].path == 'second.xml'
        !rules[0].is(rules[1])
    }

    def "invalid DSL fails with context: #configuration"() {
        when:
        evaluate(configuration)

        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains(context)
        error.message.contains(detail)

        where:
        configuration                                                       | context                          | detail
        'TYPO_RULE {}'                                                      | 'TYPO_RULE'                      | 'Unknown rule identifier'
        'TYPO_RULE'                                                         | 'TYPO_RULE'                      | 'Unknown rule identifier'
        'LOGGER_ATTRIBUTES_RULE {}'                                         | 'LOGGER_ATTRIBUTES_RULE'         | 'Unknown rule identifier'
        'README(123)'                                                       | 'README'                         | 'configuration closure'
        'README({}, {})'                                                    | 'README'                         | 'configuration closure'
        "FILE_EXISTS { paht = 'file' }"                                     | 'FILE_EXISTS'                    | 'paht'
        "FILE_EXISTS { ruleId = 'other' }"                                  | 'FILE_EXISTS'                    | 'ruleId'
        "CONFIG_FILE_NAMING { caseNaming = null }"                           | 'CONFIG_FILE_NAMING'             | 'caseNaming'
        "FILE_EXISTS { path = 123 }"                                        | 'FILE_EXISTS'                    | 'expected String'
        "MULE_CONFIG_FLOW_LIMIT { flowLimit = 'two' }"                       | 'MULE_CONFIG_FLOW_LIMIT'         | 'expected Integer'
        'MULE_CONFIG_FLOW_LIMIT { flowLimit = 2.5 }'                         | 'MULE_CONFIG_FLOW_LIMIT'         | 'expected Integer'
        "LOGGER_REQUIRED_ATTRIBUTES { requiredAttributes = 'category' }"   | 'LOGGER_REQUIRED_ATTRIBUTES'     | 'expected List'
        'MUNIT_MAVEN_PLUGIN_ATTRIBUTES { coverageAttributeMap = [] }'        | 'MUNIT_MAVEN_PLUGIN_ATTRIBUTES'   | 'expected Map'
        "AUTO_DISCOVERY_EXISTS { enabled = 'false' }"                       | 'AUTO_DISCOVERY_EXISTS'          | 'expected boolean'
        'AUTO_DISCOVERY_EXISTS { enabled = null }'                          | 'AUTO_DISCOVERY_EXISTS'          | 'expected boolean'
        "README { severity = 'MJAOR' }"                                     | 'README'                         | 'expected one of'
        'README { ruleType = 42 }'                                          | 'README'                         | 'expected RuleType'
        "README { setSeverity('MJAOR') }"                                   | 'README'                         | 'expected one of'
        "CONFIG_FILE_NAMING { format = 'not-a-format' }"                     | 'CONFIG_FILE_NAMING'             | 'Invalid format'
        "COMPONENT_REQUIRED_ATTRIBUTES { attributeMatchers = [name: '['] }" | 'COMPONENT_REQUIRED_ATTRIBUTES'  | 'Invalid configuration'
        "CONSECUTIVE_LOGGERS_COUNT { excessiveLoggers = 'two' }"              | 'CONSECUTIVE_LOGGERS_COUNT'       | 'expected Integer or Map'
    }

    def "failed initialization does not add a rule"() {
        given:
        def dsl = new RulesDsl()

        when:
        dsl.CONFIG_FILE_NAMING { format = 'INVALID' }

        then:
        thrown(IllegalArgumentException)
        dsl.ruleSet.rules.empty
    }

    def "AVIO example uses discovered identifiers and configures connector lists"() {
        when:
        def config = new CompilerConfiguration(scriptBaseClass: Dsl.name)
        def dsl = new GroovyShell(this.class.classLoader, new Binding(), config)
                .evaluate(new File('AVIOGDSLRuleConfiguration.groovy')) as MuleLinterDsl
        def rules = dsl.rulesDsl.ruleSet.rules

        then:
        rules.find { it.ruleId == 'LOGGER_REQUIRED_ATTRIBUTES' }.requiredAttributes == ['category']
        rules.find { it.ruleId == 'CONNECTION_RETRY_CONFIG' }.components*.name == ['request', 'publish', 'publish-consume']
        rules.find { it.ruleId == 'CONNECTION_TIMEOUT_CONFIG' }.components*.timeoutAttribute == ['responseTimeout', 'responseTimeout', 'responseTimeout']
    }

    private MuleLinterDsl evaluate(String rules) {
        def config = new CompilerConfiguration(scriptBaseClass: Dsl.name)
        return new GroovyShell(this.class.classLoader, new Binding(), config)
                .evaluate("mule_linter { rules { ${rules} } }") as MuleLinterDsl
    }

}
