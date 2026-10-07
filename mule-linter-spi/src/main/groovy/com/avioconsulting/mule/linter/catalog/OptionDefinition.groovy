package com.avioconsulting.mule.linter.catalog

/** An explicit, composable configuration contract. No field/annotation inference. */
final class OptionDefinition {
    final String name
    final String type
    final String description
    final boolean required
    final boolean nullable
    final boolean hasDefault
    final Object defaultValue
    final OptionDefinition items
    final Map<String, OptionDefinition> fields
    final List<OptionDefinition> alternatives
    final List<String> choices
    final Class enumClass
    final Number minimum

    private OptionDefinition(Map values) {
        name = values.name
        type = values.type
        description = values.description ?: ''
        required = values.required ?: false
        nullable = values.nullable ?: false
        hasDefault = values.hasDefault ?: false
        defaultValue = freeze(values.defaultValue)
        items = values.items
        fields = Collections.unmodifiableMap(new LinkedHashMap<>(values.fields ?: [:]))
        alternatives = Collections.unmodifiableList(new ArrayList<>(values.alternatives ?: []))
        choices = Collections.unmodifiableList(new ArrayList<>(values.choices ?: []))
        enumClass = values.enumClass
        minimum = values.minimum
    }

    static OptionDefinition string(String name) { new OptionDefinition(name: name, type: 'string') }
    static OptionDefinition integer(String name) { new OptionDefinition(name: name, type: 'integer') }
    static OptionDefinition bool(String name) { new OptionDefinition(name: name, type: 'boolean') }
    static OptionDefinition list(String name, OptionDefinition items) { new OptionDefinition(name: name, type: 'array', items: items) }
    static OptionDefinition stringList(String name) { list(name, string('item')) }
    static OptionDefinition map(String name, OptionDefinition items) { new OptionDefinition(name: name, type: 'map', items: items) }
    static OptionDefinition stringMap(String name) { map(name, string('value')) }
    static OptionDefinition object(String name, List<OptionDefinition> fields) {
        if (fields*.name.toSet().size() != fields.size()) throw new IllegalArgumentException("Duplicate fields in option '$name'")
        new OptionDefinition(name: name, type: 'object', fields: fields.collectEntries { [(it.name): it] })
    }
    static OptionDefinition union(String name, OptionDefinition... alternatives) { new OptionDefinition(name: name, type: 'union', alternatives: alternatives.toList()) }
    static OptionDefinition enumeration(String name, Class enumClass) {
        new OptionDefinition(name: name, type: 'string', choices: enumClass.enumConstants*.name(), enumClass: enumClass)
    }

    OptionDefinition description(String value) { copy(description: value) }
    OptionDefinition required() { copy(required: true) }
    OptionDefinition nullable() { copy(nullable: true) }
    OptionDefinition defaultValue(Object value) { copy(hasDefault: true, defaultValue: value) }
    OptionDefinition choices(List<String> value) { copy(choices: value) }
    OptionDefinition minimum(Number value) { copy(minimum: value) }

    private OptionDefinition copy(Map changes) {
        new OptionDefinition([name: name, type: type, description: description, required: required,
            nullable: nullable, hasDefault: hasDefault, defaultValue: defaultValue, items: items,
            fields: fields, alternatives: alternatives, choices: choices, enumClass: enumClass, minimum: minimum] + changes)
    }

    Object normalize(Object value, String path = name) {
        if (value == null) {
            if (nullable && !required) return null
            throw new IllegalArgumentException("Option '$path' cannot be null")
        }
        Object normalized
        switch (type) {
            case 'string':
                if (enumClass && enumClass.isInstance(value)) value = value.name()
                if (!(value instanceof CharSequence)) invalid(path, 'String', value)
                normalized = value.toString()
                if (choices && !choices.contains(normalized)) throw new IllegalArgumentException("Invalid option '$path': expected one of $choices, got '$normalized'")
                if (enumClass) normalized = Enum.valueOf(enumClass, normalized)
                break
            case 'integer':
                if (!(value instanceof Integer)) invalid(path, 'Integer', value)
                normalized = value
                break
            case 'boolean':
                if (!(value instanceof Boolean)) invalid(path, 'boolean', value)
                normalized = value
                break
            case 'array':
                if (!(value instanceof List)) invalid(path, 'List', value)
                normalized = value.withIndex().collect { entry, index -> items.normalize(entry, "$path[$index]") }
                break
            case 'map':
                if (!(value instanceof Map)) invalid(path, 'Map', value)
                normalized = value.collectEntries { key, entry ->
                    String text = key instanceof Enum ? key.name() : key instanceof CharSequence ? key.toString() : null
                    if (text == null) throw new IllegalArgumentException("Option '$path' requires string keys")
                    if (choices && !choices.contains(text)) throw new IllegalArgumentException("Invalid key '$text' in option '$path': expected one of $choices")
                    [(text): items.normalize(entry, "$path.$text")]
                }
                break
            case 'object':
                if (!(value instanceof Map)) invalid(path, 'Map', value)
                normalized = normalizeOptions(fields, value, path)
                break
            case 'union':
                def alternative = alternatives.find { it.acceptsOuterType(value) }
                if (alternative) return alternative.normalize(value, path)
                throw new IllegalArgumentException("Invalid option '$path': expected ${alternatives.collect { it.javaTypeName.split('<')[0].tokenize('.').last() }.join(' or ')}, got ${value.getClass().simpleName}")
            default: throw new IllegalStateException("Unsupported option type '$type'")
        }
        if (minimum != null && normalized < minimum) throw new IllegalArgumentException("Option '$path' must be at least $minimum")
        normalized
    }

    private boolean acceptsOuterType(Object value) {
        switch (type) {
            case 'string': return value instanceof CharSequence || (enumClass && enumClass.isInstance(value))
            case 'integer': return value instanceof Integer
            case 'boolean': return value instanceof Boolean
            case 'array': return value instanceof List
            case 'map': case 'object': return value instanceof Map
            case 'union': return alternatives.any { it.acceptsOuterType(value) }
            default: return false
        }
    }

    static Map<String, Object> normalizeOptions(Map<String, OptionDefinition> definitions, Map supplied, String context) {
        supplied.keySet().each { name ->
            if (!definitions.containsKey(name)) throw new IllegalArgumentException("Unknown option '$name' for $context. Available options: ${definitions.keySet().sort().join(', ')}")
        }
        Map normalized = [:]
        definitions.each { name, option ->
            if (supplied.containsKey(name)) normalized[name] = option.normalize(supplied[name], "$context.$name")
            else if (option.hasDefault) normalized[name] = option.normalize(option.defaultValue, "$context.$name")
            else if (option.required) throw new IllegalArgumentException("Missing required option '$name' for $context")
        }
        normalized
    }

    String getJavaTypeName() {
        if (enumClass) return enumClass.name
        switch (type) {
            case 'string': return 'java.lang.String'
            case 'integer': return 'java.lang.Integer'
            case 'boolean': return 'boolean'
            case 'array': return "java.util.List<${items.javaTypeName}>"
            case 'map': return "java.util.Map<java.lang.String, ${items.javaTypeName}>"
            case 'object': return 'java.util.Map'
            default: return 'java.lang.Object'
        }
    }

    Map describe() {
        Map data = [type: type, description: description, required: required, nullable: nullable]
        if (hasDefault) data.defaultValue = defaultValue
        if (choices) data.choices = choices
        if (minimum != null) data.minimum = minimum
        if (items) data.items = items.describe()
        if (fields) data.fields = fields.collectEntries { name, field -> [(name): field.describe()] }
        if (alternatives) data.alternatives = alternatives.collect { it.describe() }
        data
    }

    static Object freeze(Object value) {
        if (value instanceof Map) return Collections.unmodifiableMap(value.collectEntries { key, entry -> [(key): freeze(entry)] })
        if (value instanceof List) return Collections.unmodifiableList(value.collect { freeze(it) })
        value
    }

    private static void invalid(String path, String expected, Object value) {
        throw new IllegalArgumentException("Invalid option '$path': expected $expected, got ${value.getClass().simpleName}")
    }
}
