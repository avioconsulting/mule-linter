# Plan: Parent POM Resolution with Maven Resolver

## Goal

Replace the external Maven invoker with embedded Maven Resolver (Eclipse Aether) to resolve full parent POM chains, enabling proper inheritance of properties, dependencyManagement, and pluginManagement while maintaining .m2/repository caching and settings.xml authentication.

## Why This Matters

- External Maven invoker requires Maven installation and network access, making tests flaky
- No visibility into which POM (child or parent) provided a value
- Cannot resolve properties from parent POMs without full Maven effective-POM generation
- Parent POM resolution is essential for accurate dependency version checking
- Caching parent POMs in ~/.m2/repository provides offline capability after first resolution

## Constraints

- Fail fast if any parent in chain cannot be resolved
- Must support full parent chain (child → parent → grandparent → ...)
- Use ~/.m2/settings.xml for repository authentication
- Cache resolved parents in ~/.m2/repository (standard Maven cache)
- Must be testable with constructor injection (no singletons/statics)
- Preserve backward compatibility with existing PomFile API

## Success Criteria

- Parent POM chain resolved during PomFile construction
- Properties inherited from all parents with source tracking
- dependencyManagement versions accessible with source tracking
- pluginManagement versions accessible with source tracking
- All existing tests pass without modification (backward compatibility)
- New tests verify parent resolution works correctly
- Settings.xml authentication works for private repositories
- Offline mode works once parents are cached

## Out of Scope

- Resolving actual dependencies (jars, not POMs)
- Resolving plugins (jars, not POMs)
- BOM/Import scope dependency management (future enhancement)

## Future Enhancements

### Maven Profile Support

Support for Maven profile activation would enable:
- Conditional parent POM properties based on active profiles
- Profile-specific dependencyManagement sections
- Profile-specific pluginManagement sections
- Environment-specific property resolution (e.g., `${env.DEV_PROPERTY}`)

**Implementation Approach:**
1. Parse `<profiles>` section from settings.xml and POM files
2. Support profile activation conditions:
   - `<activeByDefault>` profiles
   - `<property>` activation (e.g., `env` property matching system property)
   - `<jdk>` version activation
   - `<file>` existence activation
3. Merge profile properties/repositories before parent resolution
4. Allow users to specify active profiles via:
   - System property: `mule.linter.activeProfiles=dev,test`
   - Environment variable: `MULE_LINTER_ACTIVE_PROFILES`
   - DSL configuration in rule files

**Example Usage:**
```groovy
// In rule configuration
rules {
    profile 'dev'  // Activate 'dev' profile for resolution
    
    PomPropertyValueRule {
        propertyName = 'app.runtime'
        propertyValue = '4.9.16'  // Resolved with dev profile active
    }
}
```

**Priority:** Medium - Current implementation handles 90% of use cases without profiles

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     PomFile (Enhanced)                       │
├─────────────────────────────────────────────────────────────┤
│  - file: File (child POM)                                    │
│  - pomXml: GPathResult                                       │
│  - parent: PomFile (optional reference to parent)            │
│  - resolver: ParentPomResolver (injected via constructor)     │
├─────────────────────────────────────────────────────────────┤
│  + getPomProperty(String): PomElement         [existing]     │
│  + resolveProperty(String): ResolvedProperty  [new]          │
│  + resolveDependency(g, a): ResolvedDependency [new]         │
│  + resolvePlugin(g, a): ResolvedPlugin        [new]          │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ resolves recursively
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              ParentPomResolver (Constructor Injected)        │
├─────────────────────────────────────────────────────────────┤
│  - repositorySystem: RepositorySystem (Maven Resolver)       │
│  - session: CloseableSession                                 │
│  - localRepoDir: File (~/.m2/repository)                     │
│  - settings: Settings (from settings.xml)                    │
├─────────────────────────────────────────────────────────────┤
│  + resolve(groupId, artifactId, version,                     │
│            relativePath, childDir): File                      │
│  + resolveParentChain(childPom): List<File>                  │
│  + close(): Cleanup session                                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ downloads/caches
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   ~/.m2/repository/ (Cache)                  │
│  com/example/parent/1.0.0/parent-1.0.0.pom                  │
│  com/example/grandparent/1.0.0/grandparent-1.0.0.pom        │
└─────────────────────────────────────────────────────────────┘
```

## New Classes

### 1. ParentPomResolver

**Location:** `mule-linter-core/src/main/groovy/.../resolver/ParentPomResolver.groovy`

**Purpose:** Resolves parent POMs using Maven Resolver with settings.xml configuration

**Constructor:**
```groovy
ParentPomResolver(String localRepoPath = null)
```

**Key Methods:**
- `File resolve(String groupId, String artifactId, String version, String relativePath, File childDir)` - Resolve single parent (relative path first, then remote). Throws `ParentPomResolutionException` on failure.
- `List<File> resolveParentChain(File childPom)` - Resolve full parent chain recursively, returns list from immediate parent to oldest ancestor.
- `void close()` - Clean up Maven Resolver session

**Behavior:**
- Check relativePath first (if provided and file exists)
- Install to local repo if resolved from relativePath
- Resolve from remote repositories via Maven Resolver if not found locally
- Throw `ParentPomResolutionException` on any failure (fail fast)
- Support authentication from settings.xml

**Dependencies:**
- Maven Resolver (Eclipse Aether)
- Maven Settings Builder

---

### 2. ParentPomResolutionException

**Location:** `mule-linter-core/src/main/groovy/.../resolver/ParentPomResolutionException.groovy`

**Extends:** `RuntimeException`

**Purpose:** Custom exception for parent resolution failures with detailed context

**Fields:**
- `String failedCoordinates` - The GAV that failed (e.g., "com.test:parent:1.0.0")
- `String attemptedRelativePath` - Relative path that was tried
- `List<String> attemptedRepositories` - Repository URLs that were attempted
- `Throwable cause` - Original exception

---

### 3. ResolvedProperty

**Location:** `mule-linter-spi/src/main/groovy/.../pom/ResolvedProperty.groovy`

**Extends:** `PomElement`

**Purpose:** Property value with full resolution metadata and source tracking

**Fields:**
- `String rawValue` - Original value with `${...}` expressions (if different from resolved value)
- `PomFile sourcePom` - Reference to PomFile that defined this property
- `String sourceCoordinates` - GAV string of source POM (e.g., "com.test:parent:1.0.0")
- `int parentDepth` - 0=child, 1=parent, 2=grandparent, etc.
- `boolean isFullyResolved` - All `${...}` expressions successfully replaced
- `List<String> resolutionChain` - Chain showing resolution steps (e.g., ["${a}", "${b}", "value"])

**Methods:**
- `boolean isInherited()` - Returns true if from parent (depth > 0)
- `boolean isFromChild()` - Returns true if depth == 0

---

### 4. ResolvedDependency

**Location:** `mule-linter-spi/src/main/groovy/.../pom/ResolvedDependency.groovy`

**Purpose:** Dependency with inheritance and source tracking

**Fields:**
- `PomDependency dependency` - The dependency object
- `String version` - Resolved version string
- `boolean isVersionFromManagement` - Version came from dependencyManagement section
- `PomFile versionSource` - Which POM provided the version
- `String sourceCoordinates` - GAV of source POM
- `int parentDepth` - How many levels up (0=child, 1=parent, 2=grandparent)

**Methods:**
- `boolean isInherited()` - Returns true if version from parent

---

### 5. ResolvedPlugin

**Location:** `mule-linter-spi/src/main/groovy/.../pom/ResolvedPlugin.groovy`

**Purpose:** Plugin with inheritance and source tracking

**Fields:**
- `PomPlugin plugin` - The plugin object
- `String version` - Resolved version string
- `boolean isVersionFromManagement` - Version came from pluginManagement section
- `PomFile versionSource` - Which POM provided the version
- `String sourceCoordinates` - GAV of source POM
- `int parentDepth` - How many levels up

**Methods:**
- `boolean isInherited()` - Returns true if version from parent

---

### 6. SettingsXmlParser (Utility)

**Location:** `mule-linter-core/src/main/groovy/.../resolver/SettingsXmlParser.groovy`

**Purpose:** Parse `~/.m2/settings.xml` for repository configuration and authentication

**Methods:**
- `Settings loadSettings()` - Load from standard locations:
  1. `${user.home}/.m2/settings.xml` (user settings)
  2. `${M2_HOME}/conf/settings.xml` (global settings, if M2_HOME set)

**Features:**
- Parse servers (authentication)
- Parse repositories
- Parse mirrors
- Parse proxies

---

## Modified Classes

### 1. PomFile (Enhanced)

**Location:** `mule-linter-spi/src/main/groovy/.../pom/PomFile.groovy`

**New Constructor:**
```groovy
PomFile(File file, GPathResult pomXml, ParentPomResolver resolver)
```

**New Fields:**
- `PomFile parent` - Reference to parent POM (null if no parent)
- `ParentPomResolver resolver` - Resolver instance passed from constructor

**New Methods:**
- `ResolvedProperty resolveProperty(String name)` - Resolve property with full inheritance chain
- `ResolvedDependency resolveDependency(String groupId, String artifactId)` - Resolve dependency with inheritance
- `ResolvedPlugin resolvePlugin(String groupId, String artifactId)` - Resolve plugin with inheritance
- `List<PomFile> getParentChain()` - Get all parents in order (parent, grandparent, etc.)
- `boolean hasParent()` - Check if this POM has a parent element

**Modified Behavior:**
- Constructor calls `resolveParentChain()` to recursively build parent chain
- `getPomProperty()` remains for backward compatibility (local properties only)
- `getDependency()` enhanced to check parent dependencyManagement (backward compatible)
- `getPlugin()` enhanced to check parent pluginManagement (backward compatible)

**Implementation Details:**
```groovy
private void resolveParentChain() {
    GPathResult parentNode = pomXml.parent
    if (parentNode.isEmpty()) return
    
    // Use injected resolver
    File parentFile = resolver.resolve(
        parentNode.groupId as String,
        parentNode.artifactId as String,
        parentNode.version as String,
        parentNode.relativePath as String,
        file.parentFile
    )
    
    // Recursively create parent - it will resolve its own parent
    this.parent = new PomFile(parentFile, 
        new MuleXmlParser().parse(parentFile), 
        resolver)  // Pass same resolver down the chain
}
```

---

### 2. MuleApplication

**Location:** `mule-linter-core/src/main/groovy/.../model/MuleApplication.groovy`

**Changes:**
- Remove `getEffectivePomFile()` method (deprecated, keep for now)
- Remove Maven invoker dependency from build.gradle
- Create `ParentPomResolver` instance in constructor
- Pass resolver to `PomFile` constructor

**New Method:**
- `void cleanup()` - Close resolver on shutdown (call from finalizer or explicit cleanup)

**Constructor Changes:**
```groovy
MuleApplication(File applicationPath, Boolean useEffectivePom) {
    this.applicationPath = applicationPath
    if (!this.applicationPath.exists()) {
        throw new FileNotFoundException(APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
    }
    
    // Create resolver for this application
    ParentPomResolver resolver = new ParentPomResolver()
    
    File pFile = new File(applicationPath, POM_FILE)
    pomFile = new PomFile(pFile, 
        pFile.exists() ? new MuleXmlParser().parse(pFile) : null,
        resolver)  // Pass resolver to PomFile
    
    // ... rest of initialization
}
```

---

## Build Configuration

### Dependencies to Add

**File:** `mule-linter-core/build.gradle`

```groovy
dependencies {
    // Existing dependencies...
    
    // Maven Resolver (Eclipse Aether)
    implementation 'org.apache.maven.resolver:maven-resolver-api:1.9.18'
    implementation 'org.apache.maven.resolver:maven-resolver-impl:1.9.18'
    implementation 'org.apache.maven.resolver:maven-resolver-connector-basic:1.9.18'
    implementation 'org.apache.maven.resolver:maven-resolver-transport-http:1.9.18'
    implementation 'org.apache.maven.resolver:maven-resolver-transport-file:1.9.18'
    implementation 'org.apache.maven.resolver:maven-resolver-supplier:1.9.18'
    
    // Maven Settings
    implementation 'org.apache.maven:maven-settings:3.9.6'
    implementation 'org.apache.maven:maven-settings-builder:3.9.6'
    
    // Remove (no longer needed):
    // implementation 'org.apache.maven.shared:maven-invoker:3.1.0'
}
```

---

## Backward Compatibility

### Existing Methods Preserved

All existing methods continue to work with same behavior:
- `PomFile.getPomProperty(String)` - Returns local property only (unchanged)
- `PomFile.getPlugin(String, String)` - Returns local plugin (unchanged)
- `PomFile.getDependency(String, String)` - Returns local dependency (unchanged)
- `PomFile.getArtifactId()` - Unchanged
- `PomFile.doesExist()` - Unchanged

### New Methods Added

New methods provide extended functionality:
- `PomFile.resolveProperty(String)` - NEW: Full inheritance with source tracking
- `PomFile.resolveDependency(String, String)` - NEW: Full inheritance with source tracking
- `PomFile.resolvePlugin(String, String)` - NEW: Full inheritance with source tracking

### Migration Path for Existing Rules

Existing rules continue to work without changes:
```groovy
// Old way (still works, local only):
PomElement prop = pom.getPomProperty('munit.version')

// New way (when parent inheritance needed):
ResolvedProperty prop = pom.resolveProperty('munit.version')
println prop.value                    // "3.6.3"
println prop.sourceCoordinates        // "com.test:parent:1.0.0"
println prop.isInherited()            // true
```

---

## Testing Strategy

### Unit Tests

**Test Class:** `ParentPomResolverTest`
- Test relativePath resolution
- Test remote repository resolution (mocked)
- Test authentication from settings.xml
- Test fail-fast behavior
- Test circular parent detection

**Test Class:** `PomFileParentResolutionTest`
- Test property inheritance with mock parents
- Test dependencyManagement inheritance
- Test pluginManagement inheritance
- Test full chain resolution (child → parent → grandparent)
- Test source tracking accuracy
- Test backward compatibility (existing methods)

### Integration Tests

**Update:** `EffectivePomIntegrationTest`
- Real resolution from `ComprehensiveParentSample`
- Verify cache in ~/.m2/repository
- Verify settings.xml authentication (if test environment supports it)
- Test offline mode after initial resolution

### Example Test Cases

```groovy
def 'Property from grandparent is resolved with correct source'() {
    given:
    testApp.addMultiLevelParentSample()  // child -> parent -> grandparent
    def resolver = new ParentPomResolver(testApp.appDir.absolutePath + '/.m2')
    def pom = new PomFile(testApp.appDir, xml, resolver)
    
    when:
    ResolvedProperty result = pom.resolveProperty('grandparent.property')
    
    then:
    result.value == 'grandparent-value'
    result.sourceCoordinates == 'com.test:grandparent:1.0.0'
    result.parentDepth == 2
    result.isInherited() == true
}

def 'DependencyManagement version from parent is tracked'() {
    given:
    testApp.addComprehensiveParentSample()
    def resolver = new ParentPomResolver(testApp.appDir.absolutePath + '/.m2')
    def pom = new PomFile(testApp.appDir, xml, resolver)
    
    when:
    ResolvedDependency result = pom.resolveDependency(
        'org.mule.connectors', 'mule-http-connector')
    
    then:
    result.version == '1.10.3'
    result.isVersionFromManagement == true
    result.isInherited() == true
    result.sourceCoordinates == 'com.test:parent:1.0.0'
}

def 'Resolution fails fast when parent not found'() {
    given:
    testApp.addPomWithMissingParent()
    def resolver = new ParentPomResolver(testApp.appDir.absolutePath + '/.m2')
    
    when:
    new PomFile(testApp.appDir, xml, resolver)
    
    then:
    thrown(ParentPomResolutionException)
}
```

---

## Error Handling

### Parent Not Found

```
ParentPomResolutionException: Failed to resolve parent com.test:parent:1.0.0
  Attempted relativePath: ../pom.xml (file not found)
  Attempted repositories: 
    - https://repo.maven.apache.org/maven2 (404)
    - https://avio.jfrog.io/artifactory/mulesoft-virtual (401 Unauthorized)
  Local repository: /home/user/.m2/repository
```

### Circular Parent Reference

```
ParentPomResolutionException: Circular parent reference detected
  Chain: com.test:child:1.0.0 -> com.test:parent:1.0.0 -> com.test:child:1.0.0
  Max depth: 50
```

### Authentication Failure

```
ParentPomResolutionException: Authentication failed for repository nexus-releases
  Repository URL: https://nexus.company.com/repository/releases
  Server ID from settings.xml: nexus-releases
  Username: deployment-user (from settings.xml)
  Caused by: org.eclipse.aether.transfer.AuthenticationException: 401 Unauthorized
```

---

## Implementation Order

1. **Add dependencies** to `mule-linter-core/build.gradle`
2. **Create** `ParentPomResolver` with full chain resolution
3. **Create** `ParentPomResolutionException`
4. **Create** `SettingsXmlParser`
5. **Create** `ResolvedProperty`, `ResolvedDependency`, `ResolvedPlugin`
6. **Enhance** `PomFile` with constructor injection and parent resolution
7. **Update** `MuleApplication` to use new resolver
8. **Remove** Maven invoker dependency (after migration complete)
9. **Update** tests to verify parent resolution
10. **Update** documentation in `AGENTS.md`

---

## Documentation Updates

### AGENTS.md Additions

```markdown
## Parent POM Resolution

Parent POMs are resolved using embedded Maven Resolver (not external Maven process):
- Resolves full parent chain (child → parent → grandparent → ...)
- Uses ~/.m2/settings.xml for repository configuration and authentication
- Caches resolved parents in ~/.m2/repository (standard Maven cache)
- Only resolves parent POMs (not dependencies or plugins)

System properties:
- `mule.linter.localRepo`: Custom local repository path (default: ~/.m2/repository)

Environment variables:
- `M2_HOME`: Used to find global settings.xml

Resolution behavior:
- Properties are inherited from all parents in chain
- dependencyManagement versions available via resolveDependency()
- pluginManagement versions available via resolvePlugin()
- Source tracking shows which POM defined each element
```

---

## Risks

- **Network dependency:** Initial parent resolution requires network (unless cached)
- **Authentication complexity:** settings.xml parsing may be incomplete
- **Circular references:** Must detect and fail fast
- **Performance:** Deep parent chains could be slow (mitigated by caching)
- **Test complexity:** Tests need mock resolvers for isolation

## Mitigations

- Fail fast with clear error messages
- Comprehensive unit tests with mocked resolver
- Cache aggressively in ~/.m2/repository
- Document settings.xml limitations
- Provide escape hatch (skip parent resolution flag)
