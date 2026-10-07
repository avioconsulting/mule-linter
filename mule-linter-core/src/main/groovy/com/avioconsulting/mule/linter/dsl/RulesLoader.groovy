package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.catalog.RuleCatalog

class RulesLoader {

    // Compatibility view keyed by the historical report IDs, not a discovery mechanism.
    private static final def Map<String, Class<? extends Rule>> rulesMap = RuleCatalog.instance.definitions.collectEntries { [(it.reportId): it.ruleClass] }

    /** Legacy utility. Runtime discovery and IDE metadata now use RuleCatalog. */
    @Deprecated
    static Map<String, Class<? extends Rule>> indexRules(Collection<Class<? extends Rule>> rules) {
        Map<String, Class<? extends Rule>> indexed = [:]
        rules.toList().sort { it.name }.each { rule ->
            String id = rule.RULE_ID
            Class<? extends Rule> existing = indexed.get(id)
            if (existing != null && existing != rule) {
                throw new IllegalArgumentException("Duplicate RULE_ID '${id}' declared by ${existing.name} and ${rule.name}")
            }
            indexed.put(id, rule)
        }
        return indexed
    }
    static def Map<String, Class<? extends Rule>> getRulesMap() {
        return Collections.unmodifiableMap(rulesMap)
    }
    static def Class<Rule> getRuleClassById(String ruleId) {
        return RuleCatalog.instance.resolve(ruleId).ruleClass
    }
}
