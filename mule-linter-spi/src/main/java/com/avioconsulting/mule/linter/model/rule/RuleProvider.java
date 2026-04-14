package com.avioconsulting.mule.linter.model.rule;

import java.util.List;

/**
 * SPI for providing rules to the linter.
 * Implementations can be registered via ServiceLoader to extend the linter with custom rules.
 */
public interface RuleProvider {
    
    /**
     * Get the name of this provider.
     */
    String getProviderName();
    
    /**
     * Get all rule descriptors provided by this provider.
     * 
     * @return list of rule descriptors
     */
    List<RuleDescriptor> getRules();
}
