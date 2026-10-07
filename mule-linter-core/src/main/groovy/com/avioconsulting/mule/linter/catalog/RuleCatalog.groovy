package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.spi.RuleProvider

final class RuleCatalog {
    private static volatile RuleCatalog cached

    final List<RuleDefinition> definitions
    private final Map<String, RuleDefinition> identifiers

    RuleCatalog(Collection<RuleDefinition> definitions) {
        Map<String, RuleDefinition> indexed = [:]
        Map<String, RuleDefinition> reports = [:]
        definitions.each { definition ->
            ([definition.id] + definition.aliases).each { identifier ->
                if (!identifier?.trim()) throw new IllegalArgumentException("Empty alias for '${definition.id}'")
                if (indexed.containsKey(identifier)) throw new IllegalArgumentException("Duplicate rule identifier '$identifier' claimed by ${indexed[identifier].ruleClass.name} and ${definition.ruleClass.name}")
                indexed[identifier] = definition
            }
            if (reports.containsKey(definition.reportId)) throw new IllegalArgumentException("Duplicate report ID '${definition.reportId}' claimed by ${reports[definition.reportId].ruleClass.name} and ${definition.ruleClass.name}")
            reports[definition.reportId] = definition
        }
        this.definitions = Collections.unmodifiableList(definitions.toList().sort { it.id })
        this.identifiers = Collections.unmodifiableMap(indexed)
    }

    static RuleCatalog getInstance() {
        RuleCatalog existing = cached
        if (existing != null) return existing
        synchronized (RuleCatalog) {
            if (cached == null) {
                try {
                    cached = new RuleCatalog(ServiceLoader.load(RuleProvider, RuleCatalog.classLoader).collectMany { it.ruleDefinitions.toList() })
                } catch (ServiceConfigurationError e) {
                    throw new IllegalStateException("Unable to load rule providers: ${e.message}", e)
                }
            }
            return cached
        }
    }

    RuleDefinition resolve(String identifier) {
        RuleDefinition definition = identifiers[identifier]
        if (definition == null) throw new IllegalArgumentException("Unknown rule identifier '$identifier'. Available rules: ${definitions*.id.join(', ')}")
        definition
    }
}
