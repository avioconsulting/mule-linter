package com.avioconsulting.mule.linter.model.rule;

import java.util.function.Supplier;

/**
 * Descriptor for a rule, containing metadata and a factory for creating instances.
 */
public class RuleDescriptor {
    
    private final String ruleId;
    private final String displayName;
    private final String description;
    private final RuleSeverity defaultSeverity;
    private final RuleType ruleType;
    private final Supplier<? extends Rule> factory;
    
    public RuleDescriptor(String ruleId, String displayName, String description,
                          RuleSeverity defaultSeverity, RuleType ruleType,
                          Supplier<? extends Rule> factory) {
        this.ruleId = ruleId;
        this.displayName = displayName;
        this.description = description;
        this.defaultSeverity = defaultSeverity;
        this.ruleType = ruleType;
        this.factory = factory;
    }
    
    /**
     * Create a simplified descriptor with just ID and factory.
     */
    public RuleDescriptor(String ruleId, Supplier<? extends Rule> factory) {
        this(ruleId, ruleId, "", RuleSeverity.MINOR, RuleType.CODE_SMELL, factory);
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public RuleSeverity getDefaultSeverity() {
        return defaultSeverity;
    }
    
    public RuleType getRuleType() {
        return ruleType;
    }
    
    /**
     * Create a new instance of the rule.
     */
    public Rule createRule() {
        return factory.get();
    }
}
