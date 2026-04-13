package com.avioconsulting.mule.linter.model.pom

/**
 * Represents a resolved plugin with source tracking.
 * Provides information about whether the plugin is inherited
 * and where its version came from.
 */
class ResolvedPlugin {
    
    /**
     * The plugin object
     */
    PomPlugin plugin
    
    /**
     * The resolved version string
     */
    String version
    
    /**
     * True if version came from pluginManagement section
     */
    boolean isVersionFromManagement
    
    /**
     * The PomFile that provided the version
     */
    PomFile versionSource
    
    /**
     * GAV coordinates of the source POM
     */
    String sourceCoordinates
    
    /**
     * Depth in parent chain: 0=child, 1=parent, 2=grandparent, etc.
     */
    int parentDepth
    
    /**
     * Returns true if this plugin version is inherited from a parent
     */
    boolean isInherited() {
        return parentDepth > 0
    }
    
    /**
     * Returns true if this plugin is defined directly in child POM
     */
    boolean isFromChild() {
        return parentDepth == 0
    }
    
    /**
     * Gets the display name for the source
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
     * Gets the full source description including management info
     */
    String getFullSourceDescription() {
        String source = getSourceDisplayName()
        if (isVersionFromManagement) {
            source += ' (pluginManagement)'
        }
        return source
    }
    
    @Override
    String toString() {
        return "ResolvedPlugin{groupId='${plugin?.groupId}', " +
               "artifactId='${plugin?.artifactId}', version='${version}', " +
               "source='${sourceCoordinates}', depth=${parentDepth}, " +
               "inherited=${isInherited()}, fromManagement=${isVersionFromManagement}}"
    }
}
