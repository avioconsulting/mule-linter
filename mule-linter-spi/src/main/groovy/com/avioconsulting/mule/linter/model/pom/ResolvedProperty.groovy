package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.model.Namespace
import groovy.xml.slurpersupport.GPathResult

/**
 * Represents a resolved property value with full source tracking.
 * Extends PomElement to provide inheritance information.
 */
class ResolvedProperty extends PomElement {
    
    /**
     * The raw value with ${...} expressions (if different from resolved value)
     */
    String rawValue
    
    /**
     * Reference to the PomFile that defined this property
     */
    PomFile sourcePom
    
    /**
     * GAV coordinates of the source POM (e.g., "com.test:parent:1.0.0")
     */
    String sourceCoordinates
    
    /**
     * Depth in parent chain: 0=child, 1=parent, 2=grandparent, etc.
     */
    int parentDepth
    
    /**
     * Chain showing resolution steps, e.g., ["${a}", "${b}", "final-value"]
     */
    List<String> resolutionChain
    
    /**
     * Returns true if this property is inherited from a parent POM
     */
    boolean isInherited() {
        return parentDepth > 0
    }
    
    /**
     * Returns true if this property is defined in the child POM
     */
    boolean isFromChild() {
        return parentDepth == 0
    }
    
    /**
     * Returns true if all ${...} expressions have been resolved
     * Checks the resolved value (not rawValue) for unresolved markers
     */
    boolean isFullyResolved() {
        return value && !value.contains('${')
    }
    
    /**
     * Gets the display name for the source (e.g., "child", "parent", "grandparent")
     */
    String getSourceDisplayName() {
        switch (parentDepth) {
            case 0: return 'child'
            case 1: return 'parent'
            case 2: return 'grandparent'
            default: return "ancestor-${parentDepth}"
        }
    }
    
    /**
     * Creates a simple string representation for debugging
     */
    @Override
    String toString() {
        return "ResolvedProperty{name='${name}', value='${value}', rawValue='${rawValue}', " +
               "source='${sourceCoordinates}', depth=${parentDepth}, " +
               "inherited=${isInherited()}, fullyResolved=${isFullyResolved()}}"
    }
}
