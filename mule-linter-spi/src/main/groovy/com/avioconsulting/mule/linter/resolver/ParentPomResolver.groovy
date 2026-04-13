package com.avioconsulting.mule.linter.resolver

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.apache.maven.settings.Settings
import org.eclipse.aether.util.repository.AuthenticationBuilder
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.supplier.RepositorySystemSupplier

/**
 * Resolves parent POMs using Maven Resolver (Eclipse Aether).
 * Supports full parent chain resolution with .m2/repository caching
 * and settings.xml authentication.
 * 
 * Uses a shared instance pattern to avoid expensive Maven Resolver
 * initialization for every PomFile.
 */
class ParentPomResolver {
    
    // Shared instance holder for lazy initialization
    private static class Holder {
        static final ParentPomResolver INSTANCE = new ParentPomResolver()
    }
    
    /**
     * Returns the shared ParentPomResolver instance.
     * This avoids expensive Maven Resolver initialization per test.
     */
    static ParentPomResolver getInstance() {
        return Holder.INSTANCE
    }
    
    private final RepositorySystem repositorySystem
    private final RepositorySystemSession session
    private final File localRepositoryDir
    private final Settings settings
    private final SettingsXmlParser settingsParser
    
    /**
     * Creates a new ParentPomResolver with the specified local repository.
     * @param localRepoPath Optional custom local repository path. Defaults to ~/.m2/repository
     */
    ParentPomResolver(String localRepoPath = null) {
        this.localRepositoryDir = new File(localRepoPath ?: 
            "${System.getProperty('user.home')}/.m2/repository")
        
        this.settingsParser = new SettingsXmlParser()
        this.settings = settingsParser.loadSettings()
        
        // Initialize Maven Resolver - expensive operation
        this.repositorySystem = new RepositorySystemSupplier().get()
        this.session = createSession(repositorySystem)
    }
    
    /**
     * Resolves a single parent POM.
     * First tries relativePath, then resolves from remote repositories.
     * 
     * @param groupId Parent groupId
     * @param artifactId Parent artifactId
     * @param version Parent version
     * @param relativePath Optional relativePath from child POM
     * @param childDir Directory containing child POM
     * @return Resolved parent POM file in local repository
     * @throws ParentPomResolutionException if parent cannot be resolved
     */
    File resolve(String groupId, String artifactId, String version, 
                 String relativePath, File childDir) {
        
        String coordinates = "${groupId}:${artifactId}:${version}"
        List<String> attemptedPaths = []
        List<String> attemptedRepositories = []
        
        // 1. Try relative path first if provided
        if (relativePath) {
            File relativePom = resolveRelativePath(relativePath, childDir)
            attemptedPaths << relativePath
            
            if (relativePom?.exists()) {
                // Install to local repo for caching
                return installToLocalRepo(relativePom, groupId, artifactId, version)
            }
        }
        
        // 2. Check if already in local repository
        File cachedPom = getCachedPom(groupId, artifactId, version)
        if (cachedPom.exists()) {
            return cachedPom
        }
        
        // 3. Resolve from remote repositories
        try {
            return resolveFromRemote(groupId, artifactId, version, attemptedRepositories)
        } catch (Exception e) {
            throw new ParentPomResolutionException(
                "Failed to resolve parent ${coordinates}",
                coordinates,
                relativePath,
                attemptedPaths,
                attemptedRepositories,
                e
            )
        }
    }
    
    /**
     * Resolves the full parent chain for a given POM file.
     * Returns list from immediate parent to oldest ancestor.
     * 
     * @param childPomFile The child POM file to resolve parents for
     * @return List of resolved parent POM files (may be empty if no parent)
     * @throws ParentPomResolutionException if any parent cannot be resolved
     */
    List<File> resolveParentChain(File childPomFile) {
        List<File> chain = []
        File currentPom = childPomFile
        Set<String> visited = [] // Prevent circular dependencies
        int maxDepth = 50 // Safety limit
        
        for (int depth = 0; depth < maxDepth; depth++) {
            ParentReference parentRef = extractParentReference(currentPom)
            if (!parentRef) break
            
            // Check for circular dependency
            if (visited.contains(parentRef.coordinates)) {
                throw new ParentPomResolutionException(
                    "Circular parent reference detected: ${parentRef.coordinates} appears twice in chain",
                    parentRef.coordinates,
                    parentRef.relativePath,
                    [],
                    [],
                    null
                )
            }
            
            visited.add(parentRef.coordinates)
            
            File parentPom = resolve(
                parentRef.groupId,
                parentRef.artifactId,
                parentRef.version,
                parentRef.relativePath,
                currentPom.parentFile
            )
            
            chain.add(parentPom)
            currentPom = parentPom
        }
        
        return chain
    }
    
    /**
     * Closes the resolver and cleans up resources.
     */
    void close() {
        // RepositorySystemSession is auto-closeable
        if (session instanceof Closeable) {
            ((Closeable) session).close()
        }
    }
    
    private RepositorySystemSession createSession(RepositorySystem system) {
        LocalRepository localRepo = new LocalRepository(localRepositoryDir)
        
        // Create a simple session using Maven's default session setup
        org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession()
    }
    
    private File resolveRelativePath(String relativePath, File childDir) {
        // Normalize the relative path
        String normalized = relativePath.replaceAll('\\\\', '/')
        return new File(childDir, normalized)
    }
    
    private File getCachedPom(String groupId, String artifactId, String version) {
        // Standard Maven local repo path: groupId/artifactId/version/artifactId-version.pom
        String groupPath = groupId.replace('.', '/')
        return new File(localRepositoryDir, 
            "${groupPath}/${artifactId}/${version}/${artifactId}-${version}.pom")
    }
    
    private File installToLocalRepo(File pomFile, String groupId, String artifactId, String version) {
        // For now, just return the file. In full implementation, would use maven-installer
        // to properly install with checksums
        File cachedPom = getCachedPom(groupId, artifactId, version)
        
        if (!cachedPom.exists() || pomFile.lastModified() > cachedPom.lastModified()) {
            cachedPom.parentFile.mkdirs()
            pomFile.withInputStream { input ->
                cachedPom.withOutputStream { output ->
                    output << input
                }
            }
        }
        
        return cachedPom
    }
    
    private File resolveFromRemote(String groupId, String artifactId, String version,
                                   List<String> attemptedRepositories) {
        Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, 'pom', version)
        
        List<RemoteRepository> repositories = buildRemoteRepositories(attemptedRepositories)
        
        ArtifactRequest request = new ArtifactRequest()
        request.setArtifact(pomArtifact)
        request.setRepositories(repositories)
        
        ArtifactResult result = repositorySystem.resolveArtifact(session, request)
        
        if (!result.isResolved()) {
            throw new RuntimeException("Artifact not resolved: ${groupId}:${artifactId}:${version}")
        }
        
        return result.getArtifact().getFile()
    }
    
    private List<RemoteRepository> buildRemoteRepositories(List<String> attemptedRepos) {
        List<RemoteRepository> repos = []
        
        // Add Maven Central as default
        repos.add(new RemoteRepository.Builder('central', 'default', 
            'https://repo.maven.apache.org/maven2/').build())
        attemptedRepos << 'https://repo.maven.apache.org/maven2/'
        
        // Add repositories from settings.xml
        settings?.repositories?.each { repo ->
            RemoteRepository.Builder builder = new RemoteRepository.Builder(
                repo.id, 'default', repo.url)
            
            // Add authentication if configured
            def server = settings.servers?.find { it.id == repo.id }
            if (server?.username && server?.password) {
                builder.setAuthentication(new AuthenticationBuilder()
                    .addUsername(server.username)
                    .addPassword(server.password)
                    .build())
            }
            
            repos.add(builder.build())
            attemptedRepos << repo.url
        }
        
        return repos
    }
    
    private ParentReference extractParentReference(File pomFile) {
        try {
            def xml = new XmlSlurper().parse(pomFile)
            def parentNode = xml.parent
            
            if (parentNode.isEmpty()) {
                return null
            }
            
            return new ParentReference(
                groupId: parentNode.groupId as String,
                artifactId: parentNode.artifactId as String,
                version: parentNode.version as String,
                relativePath: parentNode.relativePath as String ?: '../pom.xml',
                coordinates: "${parentNode.groupId}:${parentNode.artifactId}:${parentNode.version}"
            )
        } catch (Exception e) {
            return null
        }
    }
    
    /**
     * Internal data class for parent reference information
     */
    private static class ParentReference {
        String groupId
        String artifactId
        String version
        String relativePath
        String coordinates
    }
}
