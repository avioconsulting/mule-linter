package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class RulesLoader {

    private static final def Map<String, Class<? extends Rule>> rulesMap = [:]
    static {
        //This loads all Rule classes shipped with core.
        //TODO: Find a way to load all classes from external library that can have different package.
        Reflections rf = new Reflections(new ConfigurationBuilder()
                .forPackages("com.", "org.", "io.")
                .setExpandSuperTypes(false)
                .addScanners(Scanners.values()))
        def rules = rf.getSubTypesOf(Rule.class)
        //Look for field named RULE_ID
        rulesMap.putAll(indexRules(rules))
    }

    /** Shared by runtime discovery and GDSL generation; IDs must identify exactly one class. */
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
        return rulesMap.get(ruleId)
    }
}
