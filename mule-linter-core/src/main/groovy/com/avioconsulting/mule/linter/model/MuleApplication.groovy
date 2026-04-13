package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.parser.JsonSlurper
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import com.avioconsulting.mule.linter.resolver.ParentPomResolver
import org.apache.groovy.json.internal.JsonMap
import org.yaml.snakeyaml.Yaml

class MuleApplication implements Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'MuleApplication directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String README = 'README.md'
    static final String PROPERTY_PATH = 'src/main/resources'
    static final String CONFIGURATION_PATH = 'src/main/mule'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    ReadmeFile readmeFile
    String name
    GitIgnoreFile gitignoreFile
    MuleArtifact muleArtifact
    
    /**
     * Resolver for parent POM resolution. May be null if resolution is disabled.
     */
    private ParentPomResolver parentPomResolver

    MuleApplication(File applicationPath) {
        this(applicationPath, true)
    }

    MuleApplication(File applicationPath, Boolean resolveParents) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException(APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        
        File pFile = new File(applicationPath, POM_FILE)
        def pomXml = pFile.exists() ? new MuleXmlParser().parse(pFile) : null
        
        // Create PomFile (even if POM doesn't exist)
        this.pomFile = new PomFile(pFile, pomXml)
        
        // Resolve parent chain if enabled and POM exists
        if (resolveParents && pFile.exists() && pomXml) {
            resolveParentChain()
        }
        
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
        readmeFile = new ReadmeFile(applicationPath, README)
        this.name = pomFile.artifactId ?: applicationPath.name

        loadPropertyFiles()
        loadConfigurationFiles()
        loadMuleArtifact()
    }

    /**
     * Resolves the parent POM chain for this application.
     * Creates a ParentPomResolver and uses it to find and link all parent POMs.
     * 
     * If parent resolution fails, logs a warning and continues without parent inheritance.
     * This ensures the application can still be analyzed even if parent POMs can't be resolved.
     */
    private void resolveParentChain() {
        try {
            parentPomResolver = new ParentPomResolver()
            
            def parentCoords = pomFile.getParentCoordinates()
            if (!parentCoords) {
                return // No parent to resolve
            }
            
            // Resolve the parent POM
            File parentPomFile = parentPomResolver.resolve(
                parentCoords.groupId,
                parentCoords.artifactId,
                parentCoords.version,
                parentCoords.relativePath,
                applicationPath
            )
            
            // Create parent PomFile (recursively resolves its own parent)
            def parentXml = new MuleXmlParser().parse(parentPomFile)
            PomFile parentPom = new PomFile(parentPomFile, parentXml)
            
            // Recursively resolve parent's parent chain
            resolveParentParents(parentPom, parentPomResolver)
            
            // Link parent to this POM
            pomFile.parent = parentPom
            
        } catch (Exception e) {
            // Log warning and continue without parent resolution
            System.err.println("Warning: Failed to resolve parent POM chain: ${e.message}")
            System.err.println("Continuing without parent inheritance. Some rules may not work correctly.")
            
            // Close resolver on error
            parentPomResolver?.close()
            parentPomResolver = null
            // Don't re-throw - allow application to continue without parent
        }
    }
    
    /**
     * Recursively resolves parents for a given PomFile.
     * Used to build the complete parent chain.
     */
    private void resolveParentParents(PomFile pom, ParentPomResolver resolver) {
        def parentCoords = pom.getParentCoordinates()
        if (!parentCoords) {
            return // No more parents
        }
        
        // Resolve the grandparent
        File grandparentFile = resolver.resolve(
            parentCoords.groupId,
            parentCoords.artifactId,
            parentCoords.version,
            parentCoords.relativePath,
            pom.file.parentFile
        )
        
        // Create grandparent PomFile
        def grandparentXml = new MuleXmlParser().parse(grandparentFile)
        PomFile grandparentPom = new PomFile(grandparentFile, grandparentXml)
        
        // Link to parent
        pom.parent = grandparentPom
        
        // Continue recursively
        resolveParentParents(grandparentPom, resolver)
    }

    /**
     * Cleans up resources used by this application.
     * Should be called when done to close the parent POM resolver.
     */
    void cleanup() {
        parentPomResolver?.close()
        parentPomResolver = null
    }

    void loadPropertyFiles() {
        File resourcePath = new File(applicationPath, PROPERTY_PATH)
        if (resourcePath.exists()) {
            resourcePath.traverse(nameFilter: ~/.*.properties/) { file->
                propertyFiles.add(new PropertyFile(file))
            }
            resourcePath.traverse(nameFilter: ~/.*.yaml/) { file->
                propertyFiles.add(new PropertyFile(file))
            }
        }
    }

    void loadConfigurationFiles() {
        File configurationPath = new File(applicationPath, CONFIGURATION_PATH)
        if (configurationPath.exists()) {
            configurationPath.eachFileMatch(~/.*.xml/) { file ->
                configurationFiles.add(new ConfigurationFile(file))
            }

            configurationPath.eachDirRecurse { dir ->
                dir.eachFileMatch(~/.*.xml/) { file ->
                    configurationFiles.add(new ConfigurationFile(file))
                }
            }
        }
    }

    List<MuleComponent> getGlobalConfigs() {
        return configurationFiles.collect {it.findGlobalConfigs() }.flatten()
    }

    List<MuleComponent> findComponents(String componentType, String namespace) {
        return configurationFiles.collect { it.findComponents(componentType, namespace) }.flatten()
    }

    List<FlowComponent> getFlows() {
        return configurationFiles.collect { it.flows }.flatten()
    }

    List<FlowComponent> getSubFlows() {
        return configurationFiles.collect { it.subFlows }.flatten()
    }

    List<FlowComponent> getAllFlows() {
        return flows + subFlows
    }

    List<MuleComponent> getFlowrefs() {
        return configurationFiles.collect { it.flowrefs }.flatten()
    }

    void loadMuleArtifact() {
        File file = new File(applicationPath, MuleArtifact.MULE_ARTIFACT_JSON)
        def content = null
        if (file.exists()) {
            JsonSlurper slurper = new JsonSlurper()
            content = slurper.parse(file) as JsonMap
        }
        muleArtifact = new MuleArtifact(file, content)
    }

    public File getApplicationPath() {
        return applicationPath
    }

    PomFile getPomFile() {
        return pomFile
    }

    GitIgnoreFile getGitignoreFile() {
        return gitignoreFile
    }

    ReadmeFile getReadmeFile() {
        return readmeFile
    }


    Boolean hasFile(String filename) {
        File file = new File(applicationPath, filename)
        return file.exists()
    }

    String getName() {
        return name
    }

    List<PropertyFile> getPropertyFiles() {
        return propertyFiles
    }

    MuleArtifact getMuleArtifact() {
        return muleArtifact
    }

}
