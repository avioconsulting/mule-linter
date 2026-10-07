package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.model.rule.Rule

/** Validated configuration independent of an application or executable rule instance. */
final class RuleSpecification {
    final RuleDefinition definition
    final Map<String, Object> options

    RuleSpecification(RuleDefinition definition, Map supplied) {
        this.definition = definition
        this.options = OptionDefinition.freeze(OptionDefinition.normalizeOptions(definition.options, supplied, "rule '${definition.reportId}' (${definition.id})")) as Map
    }

    Rule instantiate() {
        // Factories receive a fresh mutable copy; rule init() can legitimately augment maps/lists.
        definition.instantiate(mutableCopy(options) as Map)
    }

    private static Object mutableCopy(Object value) {
        if (value instanceof Map) return value.collectEntries { key, entry -> [(key): mutableCopy(entry)] }
        if (value instanceof List) return value.collect { mutableCopy(it) }
        value
    }
}
