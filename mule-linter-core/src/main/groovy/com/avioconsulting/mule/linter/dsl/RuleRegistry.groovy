package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.RuleDescriptor
import com.avioconsulting.mule.linter.model.rule.RuleProvider

/**
 * Rule registry that aggregates rules from all providers.
 * Replaces the reflection-based RulesLoader.
 */
class RuleRegistry {
    
    private static final Map<String, RuleDescriptor> RULES = [:]
    private static volatile boolean initialized = false
    
    /**
     * Initialize the registry by loading all RuleProviders via ServiceLoader
     * and registering built-in rules.
     */
    static synchronized void initialize() {
        if (initialized) {
            return
        }
        
        // Clear any existing registrations
        RULES.clear()
        
        // Load built-in provider explicitly (avoiding ServiceLoader for core rules)
        BuiltInRuleProvider builtInProvider = new BuiltInRuleProvider()
        builtInProvider.getRules().each { descriptor ->
            RULES[descriptor.ruleId] = descriptor
        }
        
        // Load external providers via ServiceLoader
        ServiceLoader.load(RuleProvider.class).each { provider ->
            provider.getRules().each { descriptor ->
                // External providers can override built-in rules
                RULES[descriptor.ruleId] = descriptor
            }
        }
        
        initialized = true
    }
    
    /**
     * Get a rule descriptor by ID.
     * @param ruleId the rule identifier (e.g., "AZURE_PIPELINES_EXISTS")
     * @return the rule descriptor, or null if not found
     */
    static RuleDescriptor getRuleDescriptor(String ruleId) {
        if (!initialized) {
            initialize()
        }
        return RULES[ruleId]
    }
    
    /**
     * Check if a rule is registered.
     */
    static boolean hasRule(String ruleId) {
        if (!initialized) {
            initialize()
        }
        return RULES.containsKey(ruleId)
    }
    
    /**
     * Get all registered rule IDs.
     */
    static Set<String> getRegisteredRuleIds() {
        if (!initialized) {
            initialize()
        }
        return new HashSet<>(RULES.keySet())
    }
    
    /**
     * Register a rule descriptor programmatically.
     * This can be used for testing or dynamic rule registration.
     */
    static void register(RuleDescriptor descriptor) {
        RULES[descriptor.ruleId] = descriptor
    }
    
    /**
     * Clear all registrations. Primarily for testing.
     */
    static void reset() {
        RULES.clear()
        initialized = false
    }
}
