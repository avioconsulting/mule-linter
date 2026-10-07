package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.catalog.RuleDefinition

/** DSL assignments build configuration, not mutable executable rules. */
class RuleOptions {
    private final RuleDefinition definition
    private final Map<String, Object> supplied = [:]

    RuleOptions(RuleDefinition definition) { this.definition = definition }

    Map<String, Object> suppliedOptions() { new LinkedHashMap<>(supplied) }

    @Override
    Object getProperty(String name) {
        def option = definition.options[name]
        if (option == null) unknown(name)
        if (!supplied.containsKey(name) && option.hasDefault) supplied[name] = option.normalize(option.defaultValue, "rule '${definition.reportId}'.${name}")
        supplied[name]
    }

    @Override
    void setProperty(String name, Object value) {
        def option = definition.options[name]
        if (option == null) unknown(name)
        supplied[name] = option.normalize(value, "rule '${definition.reportId}'.${name}")
    }

    def methodMissing(String name, args) {
        if (name.startsWith('set') && name.length() > 3 && args.length == 1) {
            setProperty(java.beans.Introspector.decapitalize(name.substring(3)), args[0])
            return null
        }
        if (name.startsWith('get') && name.length() > 3 && args.length == 0) {
            return getProperty(java.beans.Introspector.decapitalize(name.substring(3)))
        }
        throw new IllegalArgumentException("Unsupported configuration method '$name' for rule '${definition.reportId}'")
    }

    private void unknown(String name) {
        throw new IllegalArgumentException("Unknown option '$name' for rule '${definition.reportId}'. Available options: ${definition.options.keySet().sort().join(', ')}")
    }
}
