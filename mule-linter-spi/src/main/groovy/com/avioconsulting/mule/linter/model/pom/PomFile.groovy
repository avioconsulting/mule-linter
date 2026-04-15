package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.model.ProjectFile
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import com.avioconsulting.mule.linter.resolver.ParentPomResolver
import groovy.xml.slurpersupport.GPathResult

/**
 * Represents a Maven POM file with support for parent POM inheritance.
 * Provides access to properties, dependencies, and plugins with full inheritance chain support.
 * 
 * Parent resolution is explicit - callers must invoke resolveParents(ParentPomResolver)
 * before calling resolveProperty/resolveDependency/resolvePlugin if they want parent
 * inheritance. This avoids expensive Maven Resolver initialization for tests and 
 * applications that don't need parent inheritance.
 * 
 * Usage:
 *   PomFile pomFile = new PomFile(file, xml)
 *   pomFile.resolveParents(ParentPomResolver.getInstance())  // Optional: for parent inheritance
 *   ResolvedProperty prop = pomFile.resolveProperty("some.prop")  // Searches local + parents if resolved
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
     * Tracks whether parent resolution has been attempted.
     * Prevents re-running resolution logic when there is no parent.
     */
    private boolean parentsResolved = false
    
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
     * Returns the groupId from the POM, falling back to parent if not declared locally.
     * In Maven, child POMs often omit groupId and inherit it from parent.
     */
    String getGroupId() {
        return getPomValueOrParentFallback('groupId')
    }

    /**
     * Returns the version from the POM, falling back to parent if not declared locally.
     * In Maven, child POMs often omit version and inherit it from parent.
     */
    String getVersion() {
        return getPomValueOrParentFallback('version')
    }
    
    /**
     * Gets a value from the POM, with fallback to parent if not present locally.
     * This follows Maven's inheritance model where child POMs can omit groupId/version.
     * 
     * @param propertyName The property to get (e.g., 'groupId', 'version')
     * @return The value from local POM or parent, or empty string if neither has it
     */
    private String getPomValueOrParentFallback(String propertyName) {
        if (!exists) {
            return ''
        }

        // Try local value first
        String value = pomXml.getProperty(propertyName)
        if (value?.trim()) {
            return value
        }

        // Fall back to parent if available
        GPathResult parentNode = pomXml.parent
        if (parentNode?.isEmpty()) {
            return ''
        }

        String parentValue = parentNode.getProperty(propertyName)
        return parentValue?.trim() ?: ''
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
     * Get all parents in chain from immediate parent to oldest ancestor.
     * Returns empty list if parents haven't been resolved yet.
     */
    List<PomFile> getParentChain() {
        List<PomFile> chain = []
        PomFile current = this.parent
        while (current) {
            chain.add(current)
            current = current.parent
        }
        return chain
    }
    
    /**
     * Resolves and links parent POM chain using provided resolver.
     * Parents are parsed with MuleXmlParser for consistent line number support.
     * Populates the `parent` field with the immediate parent.
     * 
     * @param resolver ParentPomResolver to use for resolution
     * @throws ParentPomResolutionException if parent chain cannot be resolved
     */
    void resolveParents(ParentPomResolver resolver) {
        if (parentsResolved) {
            return  // Already resolved (or attempted), no need to re-run
        }
        
        parentsResolved = true  // Mark as attempted to prevent re-runs
        
        try {
            List<PomFile> parentChain = resolver.resolveParentChain(this.file)
            if (!parentChain.isEmpty()) {
                // Link to immediate parent (already parsed and linked by resolver)
                this.parent = parentChain[0]
            }
        } catch (com.avioconsulting.mule.linter.resolver.ParentPomResolutionException e) {
            // Re-throw resolution exceptions as-is
            throw e
        } catch (Exception e) {
            // Wrap other exceptions with context
            throw new com.avioconsulting.mule.linter.resolver.ParentPomResolutionException(
                "Failed to resolve parent POM chain for ${file?.name}: ${e.message}",
                null, null, [], [], resolver.getLocalRepositoryDir(), e)
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
     * Checks pluginManagement in parents. Follows Maven's inheritance rules:
     * 1. If plugin declared in <build><plugins> with version, use that version
     * 2. If plugin declared without version, check local pluginManagement
     * 3. If still no version, check parent chain's pluginManagement
     * 4. Plugin identity (declaration) always comes from <build><plugins>, never from management only
     */
    ResolvedPlugin resolvePlugin(String groupId, String artifactId) {
        // Find local plugin declaration (if any)
        PomPlugin localPlugin = getPlugin(groupId, artifactId)
        
        // Resolve version using inheritance chain
        String version = null
        boolean fromManagement = false
        PomFile versionSource = null
        int depth = 0
        
        if (localPlugin?.version?.value) {
            // Case 1: Plugin declared with explicit version - Maven uses local version
            version = localPlugin.version.value
            fromManagement = false
            versionSource = this
            depth = 0
        } else {
            // Case 2 & 3: No explicit version, check management (local then parent chain)
            def managementResult = resolveVersionFromManagement(
                groupId, artifactId,
                { p -> p.pomXml.build?.pluginManagement?.plugins?.plugin },
                { p, node -> p.getManagedVersion(node) }
            )
            
            if (managementResult) {
                version = managementResult.version
                fromManagement = true
                versionSource = managementResult.source
                depth = managementResult.depth
            }
        }
        
        // Build result if we found a version and have a local plugin declaration
        if (version && localPlugin) {
            return buildResolvedPlugin(localPlugin, version, fromManagement, versionSource, depth)
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
     * Checks dependencyManagement in parents. Follows Maven's inheritance rules:
     * 1. If dependency declared with version, use that version (local wins)
     * 2. If no version, check local dependencyManagement
     * 3. If still no version, check parent chain's dependencyManagement
     * 4. Dependency identity (scope, exclusions, etc.) always comes from child's declaration
     */
    ResolvedDependency resolveDependency(String groupId, String artifactId) {
        // Find local dependency declaration (if any)
        PomDependency localDep = getDependency(groupId, artifactId)
        
        // Resolve version using inheritance chain
        String version = null
        boolean fromManagement = false
        PomFile versionSource = null
        int depth = 0
        
        if (localDep?.version?.value) {
            // Case 1: Dependency declared with explicit version - Maven uses local version
            version = localDep.version.value
            fromManagement = false
            versionSource = this
            depth = 0
        } else {
            // Case 2 & 3: No explicit version, check management (local then parent chain)
            def managementResult = resolveVersionFromManagement(
                groupId, artifactId,
                { p -> p.pomXml.dependencyManagement?.dependencies?.dependency },
                { p, node -> p.getManagedVersion(node) }
            )
            
            if (managementResult) {
                version = managementResult.version
                fromManagement = true
                versionSource = managementResult.source
                depth = managementResult.depth
            }
        }
        
        // Build result if we found a version and have a local dependency declaration
        if (version && localDep) {
            return buildResolvedDependency(localDep, version, fromManagement, versionSource, depth)
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
    
    /**
     * Resolves version from management sections (pluginManagement or dependencyManagement).
     * Checks local POM first, then parent chain.
     * 
     * @param groupId The groupId to search for
     * @param artifactId The artifactId to search for
     * @param managementProvider Closure that extracts management section from a PomFile
     * @param versionExtractor Closure that extracts version from a management node
     * @return Map with [version, source, depth] or null if not found
     */
    private def resolveVersionFromManagement(String groupId, String artifactId, 
                                             Closure managementProvider,
                                             Closure versionExtractor) {
        // Check local management first
        GPathResult localManaged = managementProvider(this)?.find {
            it.groupId == groupId && it.artifactId == artifactId
        }
        if (localManaged) {
            String version = versionExtractor(this, localManaged)
            if (version) {
                return [version: version, source: this, depth: 0]
            }
        }
        
        // Search parent chain
        PomFile current = this.parent
        int depth = 1
        
        while (current) {
            GPathResult parentManaged = managementProvider(current)?.find {
                it.groupId == groupId && it.artifactId == artifactId
            }
            
            if (parentManaged) {
                String version = versionExtractor(current, parentManaged)
                if (version) {
                    return [version: version, source: current, depth: depth]
                }
            }
            
            current = current.parent
            depth++
        }
        
        return null
    }
    
    /**
     * Builds a ResolvedPlugin result with consistent field population.
     */
    private ResolvedPlugin buildResolvedPlugin(PomPlugin plugin, String version,
                                               boolean fromManagement, PomFile source,
                                               int depth) {
        ResolvedPlugin result = new ResolvedPlugin()
        result.plugin = plugin
        result.version = version
        result.isVersionFromManagement = fromManagement
        result.versionSource = source
        result.sourceCoordinates = "${source.getGroupId()}:${source.getArtifactId()}:${source.getVersion()}"
        result.parentDepth = depth
        return result
    }
    
    /**
     * Builds a ResolvedDependency result with consistent field population.
     */
    private ResolvedDependency buildResolvedDependency(PomDependency dependency, String version,
                                                       boolean fromManagement, PomFile source,
                                                       int depth) {
        ResolvedDependency result = new ResolvedDependency()
        result.dependency = dependency
        result.version = version
        result.isVersionFromManagement = fromManagement
        result.versionSource = source
        result.sourceCoordinates = "${source.getGroupId()}:${source.getArtifactId()}:${source.getVersion()}"
        result.parentDepth = depth
        return result
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
