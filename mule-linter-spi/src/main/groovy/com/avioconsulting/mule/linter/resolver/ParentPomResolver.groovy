package com.avioconsulting.mule.linter.resolver

import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.transform.CompileStatic
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
import org.eclipse.aether.supplier.SessionBuilderSupplier

/**
 * Resolves parent POMs using Maven Resolver (Eclipse Aether).
 * Supports full parent chain resolution with .m2/repository caching.
 * 
 * Settings.xml support:
 * - Local repository path (settings.localRepository)
 * - Server authentication (settings.servers) for Maven Central
 * 
 * Not currently supported:
 * - Profile repositories (settings.profiles)
 * - Mirrors (settings.mirrors)
 * - Proxies (settings.proxies)
 * 
 * ARCHITECTURE NOTE - INTENTIONAL SINGLETON PATTERN:
 * 
 * This class uses a shared instance pattern (ParentPomResolver.getInstance()) rather
 * than pure constructor injection. This is a deliberate performance optimization:
 * 
 * - Maven Resolver initialization is expensive (~500ms per instance)
 * - Constructor injection would cost 500ms per MuleApplication or test class
 * - With 20 test classes: 20 × 500ms = 10 seconds overhead
 * - Singleton pattern: 500ms once for entire JVM lifecycle
 * 
     * Resource Management:
     * - JVM shutdown hook automatically calls close() on the singleton
     * - Maven goals use openScope() to close their isolated resolver before realm disposal
 * - No manual cleanup needed in application code
 * - repositorySystem.shutdown() releases HTTP pools and threads on JVM exit
 * 
 * For Testing:
 * - Integration tests: Use ParentPomResolver.getInstance() (shared, fast)
 * - Unit tests with custom config: Use new ParentPomResolver(customPath) (isolated)
 * 
 * See plans/04-parent-pom-resolution.md for detailed design rationale and rejected alternatives.
 */
class ParentPomResolver {
    private static final ThreadLocal<ResolverScope> scopes = new ThreadLocal<>()

    /** Maven plugin realms can close before JVM shutdown; release their resolver at goal completion. */
    static ResolverScope openScope() {
        def scope = new ResolverScope(scopes.get())
        scopes.set(scope)
        scope
    }

    static final class ResolverScope implements AutoCloseable {
        private final ResolverScope previous
        private ParentPomResolver resolver
        private boolean closed

        private ResolverScope(ResolverScope previous) { this.previous = previous }

        private ParentPomResolver getResolver() {
            if (closed) throw new IllegalStateException('Resolver scope is closed')
            if (resolver == null) resolver = new ParentPomResolver()
            resolver
        }

        void close() {
            if (closed) return
            if (scopes.get() != this) throw new IllegalStateException('Resolver scopes must close in reverse order on their owning thread')
            closed = true
            try {
                resolver?.close()
            } finally {
                if (previous == null) scopes.remove()
                else scopes.set(previous)
            }
        }
    }
    
    // Shared instance holder for lazy initialization
    private static class Holder {
        static final ParentPomResolver INSTANCE = new ParentPomResolver()
        
        static {
            // Register shutdown hook to clean up resources when JVM exits
            // This ensures repositorySystem.shutdown() is called exactly once
            Runtime.runtime.addShutdownHook(new Thread(new ResolverShutdown(INSTANCE), 'mule-linter-resolver-shutdown'))
        }
    }

    @CompileStatic
    private static class ResolverShutdown implements Runnable {
        private final ParentPomResolver resolver

        ResolverShutdown(ParentPomResolver resolver) { this.resolver = resolver }

        void run() { resolver.close() }
    }
    
    /**
     * Returns the shared ParentPomResolver instance.
     * This avoids expensive Maven Resolver initialization per test.
     * Inside an open scope, returns that scope's lazy, isolated instance instead.
     */
    static ParentPomResolver getInstance() {
        ResolverScope scope = scopes.get()
        if (scope != null) return scope.getResolver()
        return Holder.INSTANCE
    }
    
    private final RepositorySystem repositorySystem
    private final RepositorySystemSession.CloseableSession session
    private final File localRepositoryDir
    private final Settings settings
    private final SettingsXmlParser settingsParser
    
    /**
     * Returns the local repository directory used by this resolver.
     * @return The File representing the local Maven repository path
     */
    File getLocalRepositoryDir() {
        return localRepositoryDir
    }
    
    /**
     * Creates a new ParentPomResolver with the specified local repository.
     * @param localRepoPath Optional custom local repository path. Defaults to ~/.m2/repository
     *                      or value from 'mule.linter.localRepo' system property
     */
    ParentPomResolver(String localRepoPath = null) {
        // Load settings first to check settings.xml localRepository
        this.settingsParser = new SettingsXmlParser()
        this.settings = settingsParser.loadSettings()
        
        // Priority: 1) explicit parameter, 2) system property, 3) settings.xml, 4) default ~/.m2/repository
        String effectivePath = localRepoPath ?: 
            System.getProperty('mule.linter.localRepo') ?:
            settingsParser.getLocalRepositoryPath(settings) ?:
            "${System.getProperty('user.home')}/.m2/repository"
        this.localRepositoryDir = new File(effectivePath)
        
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
            attemptedPaths << relativePom.absolutePath  // Store absolute path for accurate error reporting
            
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
                localRepositoryDir,
                e
            )
        }
    }
    
    /**
     * Resolves the full parent chain for a given POM file.
     * Returns List<PomFile> with parents already parsed using MuleXmlParser
     * and linked together (immediate parent first, with its parent link set, etc.).
     * 
     * @param childPomFile The child POM file to resolve parents for
     * @return List of resolved parent PomFiles (immediate parent first), empty if no parent
     * @throws ParentPomResolutionException if any parent cannot be resolved
     */
    List<PomFile> resolveParentChain(File childPomFile) {
        List<PomFile> chain = []
        File currentPomFile = childPomFile
        Set<String> visited = [] // Prevent circular dependencies
        int maxDepth = 50 // Safety limit
        MuleXmlParser xmlParser = new MuleXmlParser()
        
        for (int depth = 0; depth < maxDepth; depth++) {
            ParentReference parentRef = extractParentReference(currentPomFile)
            if (!parentRef) break
            
            // Check for circular dependency
            if (visited.contains(parentRef.coordinates)) {
                throw new ParentPomResolutionException(
                    "Circular parent reference detected: ${parentRef.coordinates} appears twice in chain",
                    parentRef.coordinates,
                    parentRef.relativePath,
                    [],
                    [],
                    localRepositoryDir,
                    null
                )
            }
            
            visited.add(parentRef.coordinates)
            
            File parentPomFile = resolve(
                parentRef.groupId,
                parentRef.artifactId,
                parentRef.version,
                parentRef.relativePath,
                currentPomFile.parentFile
            )
            
            // Parse with MuleXmlParser for consistent line number support
            def parentXml = xmlParser.parse(parentPomFile)
            PomFile parentPom = new PomFile(parentPomFile, parentXml)
            chain.add(parentPom)
            
            currentPomFile = parentPomFile
        }
        
        // Link parents: first parent's parent is second, etc.
        for (int i = 0; i < chain.size() - 1; i++) {
            chain[i].parent = chain[i + 1]
        }
        
        return chain
    }
    
    /**
     * Shuts down the resolver and cleans up resources.
     * 
     * IMPORTANT: For the singleton instance (getInstance()), this is called
     * automatically via JVM shutdown hook. Do NOT call close() on the singleton
     * from individual MuleApplication instances.
     * 
     * For manually created instances (new ParentPomResolver()), call this
     * when the instance is no longer needed to release HTTP connections,
     * thread pools, and other resources.
     */
    @CompileStatic
    void close() {
        // RepositorySystem manages HTTP clients, thread pools, etc.
        // Shutdown must be called to release these resources properly
        try {
            session?.close()
        } finally {
            repositorySystem?.shutdown()
        }
    }
    
    private RepositorySystemSession.CloseableSession createSession(RepositorySystem system) {
        // Resolver 2 sessions have their own lifecycle and must close before the system.
        return new SessionBuilderSupplier(system).get()
            .withLocalRepositories(new LocalRepository(localRepositoryDir))
            .build()
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
        
        // Add Maven Central as default with authentication if configured in settings.xml
        RemoteRepository.Builder centralBuilder = new RemoteRepository.Builder('central', 'default', 
            'https://repo.maven.apache.org/maven2/')
        
        // Look for Maven Central authentication in settings.xml (server id 'central')
        def centralAuth = settings?.servers?.find { it.id == 'central' }
        if (centralAuth && centralAuth.username && centralAuth.password) {
            centralBuilder.setAuthentication(new AuthenticationBuilder()
                .addUsername(centralAuth.username)
                .addPassword(centralAuth.password)
                .build())
        }
        
        repos.add(centralBuilder.build())
        attemptedRepos << 'https://repo.maven.apache.org/maven2/'
        
        // Add other repositories from settings.xml servers (for private repos)
        settings?.servers?.each { server ->
            if (server.id != 'central' && server.configuration) {
                // Check if server has URL configuration (custom repository)
                def url = server.configuration?.getChild('url')?.value
                if (url) {
                    RemoteRepository.Builder customBuilder = new RemoteRepository.Builder(
                        server.id, 'default', url)
                    if (server.username && server.password) {
                        customBuilder.setAuthentication(new AuthenticationBuilder()
                            .addUsername(server.username)
                            .addPassword(server.password)
                            .build())
                    }
                    repos.add(customBuilder.build())
                    attemptedRepos << url
                }
            }
        }
        
        return repos
    }
    
    private ParentReference extractParentReference(File pomFile) {
        // Check if file exists first
        if (!pomFile.exists()) {
            throw new ParentPomResolutionException(
                "POM file does not exist: ${pomFile.absolutePath}",
                null, null, [pomFile.absolutePath], [], localRepositoryDir, null)
        }
        
        try {
            def xml = new XmlSlurper().parse(pomFile)
            def parentNode = xml.parent
            
            if (parentNode.isEmpty()) {
                return null  // No parent defined - this is valid
            }
            
            return new ParentReference(
                groupId: parentNode.groupId as String,
                artifactId: parentNode.artifactId as String,
                version: parentNode.version as String,
                relativePath: parentNode.relativePath as String ?: '../pom.xml',
                coordinates: "${parentNode.groupId}:${parentNode.artifactId}:${parentNode.version}"
            )
        } catch (Exception e) {
            // Fail-fast: cannot parse POM file - throw exception with context
            throw new ParentPomResolutionException(
                "Failed to parse POM file to extract parent reference: ${pomFile.absolutePath}",
                null, null, [pomFile.absolutePath], [], localRepositoryDir, e)
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
