package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.model.ProjectFile
import com.avioconsulting.mule.linter.resolver.ParentPomResolver
import groovy.xml.slurpersupport.GPathResult

/**
 * Represents a Maven POM file with support for parent POM inheritance.
 * Provides access to properties, dependencies, and plugins with full inheritance chain support.
 * 
 * Parent resolution is lazy - only performed when resolve methods are called,
 * not during construction. This avoids expensive Maven Resolver initialization
 * for tests and applications that don't need parent inheritance.
 */
class PomFile extends ProjectFile {

    public static final String POM_XML = 'pom.xml'
    static final String PROPERTIES = 'properties'
    private final GPathResult pomXml
    private final Boolean exists
    
    /**
     * Reference to parent POM (null if no parent or not yet resolved)
     */
    PomFile parent
    
    /**
     * Flag to track if parent chain has been resolved
     */
    private boolean parentChainResolved = false

    /**
     * Existing constructor - maintains backward compatibility
     * No parent resolution performed
     */
    PomFile(File file, GPathResult pomXml) {
        super(file)
        if (file.exists()) {
            exists = true
            this.pomXml = pomXml
        } else {
            exists = false
            this.pomXml = null
        }
    }

    /**
     * Convenience constructor for application directory
     */
    PomFile(File application, String fileName, GPathResult pomXml) {
        this(new File(application, fileName), pomXml)
    }

    String getArtifactId() {
        return exists ? pomXml.getProperty('artifactId') : ''
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId
    }

    Boolean doesExist() {
        return exists
    }

    String getPath() {
        return file.absolutePath
    }

    /**
     * Returns the groupId from the POM
     */
    String getGroupId() {
        return exists ? pomXml.getProperty('groupId') : ''
    }

    /**
     * Returns the version from the POM
     */
    String getVersion() {
        return exists ? pomXml.getProperty('version') : ''
    }

    /**
     * Returns parent coordinates if this POM has a parent
     */
    ParentCoordinates getParentCoordinates() {
        if (!exists) return null
        
        GPathResult parentNode = pomXml.parent
        if (parentNode.isEmpty()) {
            return null
        }
        
        return new ParentCoordinates(
            groupId: parentNode.groupId as String,
            artifactId: parentNode.artifactId as String,
            version: parentNode.version as String,
            relativePath: parentNode.relativePath as String ?: '../pom.xml'
        )
    }

    /**
     * Check if this POM has a parent
     */
    boolean hasParent() {
        return exists && !pomXml.parent.isEmpty()
    }

    /**
     * Get all parents in chain from immediate parent to oldest ancestor
     */
    List<PomFile> getParentChain() {
        // Ensure parent chain is resolved first
        lazyResolveParents()
        
        List<PomFile> chain = []
        PomFile current = this.parent
        while (current) {
            chain.add(current)
            current = current.parent
        }
        return chain
    }
    
    /**
     * Lazily resolves parent chain when needed.
     * Uses shared ParentPomResolver instance to avoid expensive initialization.
     * This method is called automatically by resolve methods.
     */
    private synchronized void lazyResolveParents() {
        if (parentChainResolved) {
            return
        }
        
        if (!hasParent()) {
            parentChainResolved = true
            return
        }
        
        try {
            // Use shared resolver instance
            ParentPomResolver resolver = ParentPomResolver.getInstance()
            resolveParentChainRecursive(this, resolver)
        } catch (Exception e) {
            // Log warning and continue without parent resolution
            System.err.println("Warning: Failed to resolve parent POM chain for ${file?.name}: ${e.message}")
        }
        
        parentChainResolved = true
    }
    
    /**
     * Recursively resolves parent chain for a given PomFile.
     */
    private static void resolveParentChainRecursive(PomFile pom, ParentPomResolver resolver) {
        if (!pom?.hasParent()) {
            return
        }
        
        def parentCoords = pom.getParentCoordinates()
        if (!parentCoords) {
            return
        }
        
        try {
            // Resolve the parent POM
            File parentPomFile = resolver.resolve(
                parentCoords.groupId,
                parentCoords.artifactId,
                parentCoords.version,
                parentCoords.relativePath,
                pom.file.parentFile
            )
            
            // Create parent PomFile
            def parentXml = new groovy.xml.XmlSlurper().parse(parentPomFile)
            PomFile parentPom = new PomFile(parentPomFile, parentXml)
            parentPom.parentChainResolved = true // Mark as resolved to avoid re-resolution
            
            // Link to child
            pom.parent = parentPom
            
            // Recursively resolve parent's parent
            resolveParentChainRecursive(parentPom, resolver)
            
        } catch (Exception e) {
            // Log but don't fail - continue without parent
            System.err.println("Warning: Could not resolve parent ${parentCoords}: ${e.message}")
        }
    }

    /**
     * Get local property only (backward compatible)
     * Does not search parent POMs
     */
    PomElement getPomProperty(String propertyName) throws IllegalArgumentException {
        GPathResult p = pomProperties[propertyName] as GPathResult
        if (p == null) {
            throw new IllegalArgumentException("Property '${propertyName}' doesn't exist")
        }

        PomElement prop = new PomElement()
        prop.name = propertyName
        prop.value = p.text()
        prop.lineNo = ArtifactDescriptor.getNodeLineNumber(p)
        return prop
    }

    /**
     * Resolve property with full parent chain support and source tracking
     * 
     * @param propertyName Name of the property to resolve
     * @return ResolvedProperty with value and source information
     * @throws IllegalArgumentException if property not found in this POM or any parent
     */
    ResolvedProperty resolveProperty(String propertyName) {
        // Ensure parent chain is resolved before searching
        lazyResolveParents()
        
        // Try this POM first
        try {
            PomElement localProp = getPomProperty(propertyName)
            ResolvedProperty result = new ResolvedProperty()
            result.name = propertyName
            result.value = localProp.value
            result.rawValue = localProp.value
            result.lineNo = localProp.lineNo
            result.sourcePom = this
            result.sourceCoordinates = "${getGroupId()}:${getArtifactId()}:${getVersion()}"
            result.parentDepth = 0
            result.resolutionChain = [localProp.value]
            return result
        } catch (IllegalArgumentException e) {
            // Not in this POM, check parents
        }
        
        // Search parent chain
        PomFile current = this.parent
        int depth = 1
        
        while (current) {
            try {
                PomElement parentProp = current.getPomProperty(propertyName)
                ResolvedProperty result = new ResolvedProperty()
                result.name = propertyName
                result.value = parentProp.value
                result.rawValue = parentProp.value
                result.lineNo = parentProp.lineNo
                result.sourcePom = current
                result.sourceCoordinates = "${current.getGroupId()}:${current.getArtifactId()}:${current.getVersion()}"
                result.parentDepth = depth
                result.resolutionChain = [parentProp.value]
                return result
            } catch (IllegalArgumentException e) {
                // Not in this parent, continue to next
            }
            
            current = current.parent
            depth++
        }
        
        throw new IllegalArgumentException("Property '${propertyName}' not found in POM or any parent")
    }

    Integer getPropertiesLineNo() {
        return ArtifactDescriptor.getNodeLineNumber(pomProperties)
    }

    /**
     * Get local plugin only (backward compatible)
     * Does not search parent POMs
     */
    PomPlugin getPlugin(String groupId, String artifactId) {
        PomPlugin plugin
        GPathResult pluginPath = pomXml.build.plugins.plugin.find {
            it.groupId == groupId && it.artifactId == artifactId
        } as GPathResult

        if (pluginPath != null && pluginPath.size() > 0) {
            plugin = new PomPlugin(pluginPath, this)
        }
        return plugin
    }

    /**
     * Resolve plugin with full parent chain support and source tracking
     * Checks pluginManagement in parents
     */
    ResolvedPlugin resolvePlugin(String groupId, String artifactId) {
        // Ensure parent chain is resolved before searching
        lazyResolveParents()
        
        // Try this POM first
        PomPlugin localPlugin = getPlugin(groupId, artifactId)
        if (localPlugin) {
            ResolvedPlugin result = new ResolvedPlugin()
            result.plugin = localPlugin
            result.version = localPlugin.version?.value
            result.isVersionFromManagement = false
            result.versionSource = this
            result.sourceCoordinates = "${getGroupId()}:${getArtifactId()}:${getVersion()}"
            result.parentDepth = 0
            return result
        }
        
        // Check this POM's pluginManagement
        GPathResult managedPlugin = pomXml.build.pluginManagement?.plugins?.plugin?.find {
            it.groupId == groupId && it.artifactId == artifactId
        }
        if (managedPlugin) {
            ResolvedPlugin result = new ResolvedPlugin()
            result.plugin = new PomPlugin(managedPlugin, this)
            result.version = getManagedVersion(managedPlugin)
            result.isVersionFromManagement = true
            result.versionSource = this
            result.sourceCoordinates = "${getGroupId()}:${getArtifactId()}:${getVersion()}"
            result.parentDepth = 0
            return result
        }
        
        // Search parent chain
        PomFile current = this.parent
        int depth = 1
        
        while (current) {
            // Check parent's pluginManagement
            GPathResult parentManaged = current.pomXml.build.pluginManagement?.plugins?.plugin?.find {
                it.groupId == groupId && it.artifactId == artifactId
            }
            
            if (parentManaged) {
                ResolvedPlugin result = new ResolvedPlugin()
                result.plugin = new PomPlugin(parentManaged, current)
                result.version = current.getManagedVersion(parentManaged)
                result.isVersionFromManagement = true
                result.versionSource = current
                result.sourceCoordinates = "${current.getGroupId()}:${current.getArtifactId()}:${current.getVersion()}"
                result.parentDepth = depth
                return result
            }
            
            current = current.parent
            depth++
        }
        
        return null
    }

    /**
     * Get local dependency only (backward compatible)
     * Does not search parent POMs
     */
    PomDependency getDependency(String groupId, String artifactId) {
        PomDependency dependency
        GPathResult dependencyPath = pomXml.dependencies.dependency.find {
            it.groupId == groupId && it.artifactId == artifactId
        } as GPathResult

        if (dependencyPath != null && dependencyPath.size() > 0) {
            dependency = new PomDependency(dependencyPath, this)
        }
        return dependency
    }

    /**
     * Resolve dependency with full parent chain support and source tracking
     * Checks dependencyManagement in parents
     */
    ResolvedDependency resolveDependency(String groupId, String artifactId) {
        // Ensure parent chain is resolved before searching
        lazyResolveParents()
        
        // Try this POM first
        PomDependency localDep = getDependency(groupId, artifactId)
        if (localDep) {
            String version = localDep.version?.value
            
            // If version not specified directly, check dependencyManagement
            if (!version) {
                GPathResult managed = pomXml.dependencyManagement?.dependencies?.dependency?.find {
                    it.groupId == groupId && it.artifactId == artifactId
                }
                if (managed) {
                    version = getManagedVersion(managed)
                }
            }
            
            if (version) {
                ResolvedDependency result = new ResolvedDependency()
                result.dependency = localDep
                result.version = version
                result.isVersionFromManagement = !localDep.version?.value
                result.versionSource = this
                result.sourceCoordinates = "${getGroupId()}:${getArtifactId()}:${getVersion()}"
                result.parentDepth = 0
                return result
            }
        }
        
        // Search parent chain's dependencyManagement
        PomFile current = this.parent
        int depth = 1
        
        while (current) {
            GPathResult parentManaged = current.pomXml.dependencyManagement?.dependencies?.dependency?.find {
                it.groupId == groupId && it.artifactId == artifactId
            }
            
            if (parentManaged) {
                ResolvedDependency result = new ResolvedDependency()
                result.dependency = new PomDependency(parentManaged, current)
                result.version = current.getManagedVersion(parentManaged)
                result.isVersionFromManagement = true
                result.versionSource = current
                result.sourceCoordinates = "${current.getGroupId()}:${current.getArtifactId()}:${current.getVersion()}"
                result.parentDepth = depth
                return result
            }
            
            current = current.parent
            depth++
        }
        
        return null
    }

    MunitMavenPlugin getMunitPlugin() {
        PomPlugin pp = getPlugin(MunitMavenPlugin.GROUP_ID, MunitMavenPlugin.ARTIFACT_ID)
        return pp == null ? null : new MunitMavenPlugin(pp.pluginXml, this)
    }

    private GPathResult getPomProperties() {
        return pomXml[PROPERTIES] as GPathResult
    }
    
    private String getManagedVersion(GPathResult managedNode) {
        String version = managedNode.version?.text()
        if (!version) return null
        
        // Resolve property reference if needed
        if (version.startsWith('${') && version.endsWith('}')) {
            String propName = version.substring(2, version.length() - 1)
            try {
                return getPomProperty(propName).value
            } catch (IllegalArgumentException e) {
                // Try parent properties
                PomFile current = this.parent
                while (current) {
                    try {
                        return current.getPomProperty(propName).value
                    } catch (IllegalArgumentException ex) {
                        current = current.parent
                    }
                }
            }
        }
        
        return version
    }
}

/**
 * Simple data class for parent POM coordinates
 */
class ParentCoordinates {
    String groupId
    String artifactId
    String version
    String relativePath
    
    @Override
    String toString() {
        return "${groupId}:${artifactId}:${version} (relativePath: ${relativePath})"
    }
}
