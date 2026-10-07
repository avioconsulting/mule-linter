package com.avioconsulting.mule.linter.catalog

/** JSON Schema is derived from installed contracts; runtime domain validation is still authoritative. */
final class ConfigurationSchema {
    static Map generate(RuleCatalog catalog = RuleCatalog.instance) {
        Map definitions = [:]
        List branches = []
        catalog.definitions.each { definition ->
            Map options = objectSchema(definition.options)
            definitions[definition.id] = options
            List required = ['rule']
            if (options.required) required.add('options')
            branches.add([
                title: definition.id, description: definition.description, type: 'object',
                additionalProperties: false, required: required,
                properties: [
                    rule: [type: 'string', enum: [definition.id] + definition.aliases,
                           description: definition.description],
                    options: ['$ref': '#/$defs/' + definition.id]
                ]
            ])
        }
        [
            '$schema': 'https://json-schema.org/draft/2020-12/schema',
            '$id': 'urn:mule-linter:configuration:1',
            title: 'Mule Linter configuration',
            description: 'Rule configuration for the installed Mule Linter catalog. Defaults are applied by the linter, not by JSON Schema.',
            type: 'object', additionalProperties: false, required: ['schemaVersion', 'rules'],
            properties: [
                schemaVersion: [type: 'integer', const: 1, description: 'Configuration format version.'],
                rules: [type: 'array', description: 'Ordered rule instances. Repeated instances are permitted.',
                        items: branches ? [oneOf: branches] : false]
            ],
            '$defs': definitions
        ]
    }

    private static Map objectSchema(Map<String, OptionDefinition> fields) {
        Map schema = [type: 'object', additionalProperties: false,
                      properties: fields.collectEntries { name, option -> [(name): optionSchema(option)] }]
        List required = fields.findAll { name, option -> option.required && !option.hasDefault }.keySet().toList()
        if (required) schema.required = required
        schema
    }

    private static Map optionSchema(OptionDefinition option) {
        Map schema
        switch (option.type) {
            case 'string':
                schema = [type: 'string']
                if (option.choices) schema.enum = option.choices
                break
            case 'integer':
                schema = [type: 'integer', minimum: option.minimum == null ? Integer.MIN_VALUE : Math.max(Integer.MIN_VALUE, option.minimum), maximum: Integer.MAX_VALUE]
                break
            case 'boolean': schema = [type: 'boolean']; break
            case 'array': schema = [type: 'array', items: optionSchema(option.items)]; break
            case 'map':
                schema = [type: 'object', additionalProperties: optionSchema(option.items)]
                if (option.choices) schema.propertyNames = [enum: option.choices]
                break
            case 'object': schema = objectSchema(option.fields); break
            case 'union': schema = [anyOf: option.alternatives.collect { optionSchema(it) }]; break
            default: throw new IllegalArgumentException("Unsupported schema option type '${option.type}'")
        }
        if (option.nullable && !option.required) schema = [anyOf: [schema, [type: 'null']]]
        if (option.description) schema.description = option.description
        if (option.hasDefault) schema.default = jsonValue(option.defaultValue)
        schema
    }

    private static Object jsonValue(Object value) {
        if (value instanceof Enum) return value.name()
        if (value instanceof Map) return value.collectEntries { key, entry -> [(key): jsonValue(entry)] }
        if (value instanceof List) return value.collect { jsonValue(it) }
        value
    }
}
