package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.rule.FileExistsRule
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class ConfigurationSchemaTest extends Specification {
    def 'schema includes every installed rule and alias with strict root and entry shapes'() {
        when:
        def schema = new JsonSlurper().parseText(JsonOutput.toJson(ConfigurationSchema.generate()))
        then:
        schema.'$schema' == 'https://json-schema.org/draft/2020-12/schema'
        schema.required == ['schemaVersion', 'rules']
        schema.additionalProperties == false
        schema.properties.schemaVersion.const == 1
        schema.'$defs'.keySet() == RuleCatalog.instance.definitions*.id.toSet()
        schema.properties.rules.items.oneOf.every { branch ->
            def definition = RuleCatalog.instance.resolve(branch.title)
            branch.properties.rule.enum == [definition.id] + definition.aliases &&
                branch.properties.options.'$ref' == '#/$defs/' + definition.id && branch.additionalProperties == false
        }
    }

    def 'required options nested objects nullable values and metadata are generated from definitions'() {
        given:
        def schema = ConfigurationSchema.generate()
        def definitions = schema.'$defs'
        expect:
        definitions['file-exists'].required == ['path']
        schema.properties.rules.items.oneOf.find { it.title == 'file-exists' }.required == ['rule', 'options']
        schema.properties.rules.items.oneOf.find { it.title == 'readme' }.required == ['rule']
        definitions['logger-required-attributes'].properties.requiredAttributes.items.type == 'string'
        definitions['logger-required-attributes'].properties.requiredAttributes.default == []
        definitions['connection-timeout-config'].properties.components.items.required == ['name', 'namespace']
        definitions['connection-timeout-config'].properties.components.items.additionalProperties == false
        definitions['connection-timeout-config'].properties.components.items.properties.timeoutAttribute.default == 'responseTimeout'
        definitions['logger-message-contents'].properties.rules.anyOf[0].propertyNames.enum == ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR']
        definitions['logger-message-contents'].properties.rules.anyOf[1].type == 'null'
        definitions['readme'].properties.severity.enum == ['BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR']
        definitions['readme'].properties.severity.default == 'CRITICAL'
        definitions['flow-subflow-component-count'].properties.maxCount.minimum == 0
        definitions['flow-subflow-component-count'].properties.maxCount.maximum == Integer.MAX_VALUE
        definitions['consecutive-loggers-count'].properties.excessiveLoggers.anyOf*.type == ['integer', 'object']
    }

    def 'schemas include supplied extension providers and do not mutate catalog metadata'() {
        given:
        def definition = new RuleDefinition('extension-file', ['EXT_FILE'], 'EXT_FILE', 'Fixture extension.',
            RuleSeverity.MINOR, RuleType.CODE_SMELL, FileExistsRule,
            [OptionDefinition.string('path').required()], { Map values -> new FileExistsRule(path: values.path) })
        def catalog = new RuleCatalog([definition])
        def before = JsonOutput.toJson(definition.describe())
        when:
        def schema = ConfigurationSchema.generate(catalog)
        then:
        schema.'$defs'.keySet() == ['extension-file'] as Set
        schema.properties.rules.items.oneOf[0].properties.rule.enum == ['extension-file', 'EXT_FILE']
        before == JsonOutput.toJson(definition.describe())
    }
}
