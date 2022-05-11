package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.parser.JsonSlurper
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import org.apache.groovy.json.internal.JsonMap

class MuleApplication implements Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'MuleApplication directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String README = 'README.md'
    static final String JENKINS_FILE = JenkinsFile.JENKINSFILE
    static final String AZURE_PIPELINES_FILE = AzurePipelinesFile.AZURE_PIPELINES
    static final String PROPERTY_PATH = 'src/main/resources'
    static final String CONFIGURATION_PATH = 'src/main/mule'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    ReadmeFile readmeFile
    JenkinsFile jenkinsFile
    AzurePipelinesFile azurePipelinesFile
    String name
    GitIgnoreFile gitignoreFile
    MuleArtifact muleArtifact

    MuleApplication(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        File pFile = new File(applicationPath, POM_FILE);
        pomFile = new PomFile(pFile, pFile.exists() ? new MuleXmlParser().parse(pFile) : null)
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
        readmeFile = new ReadmeFile(applicationPath, README)
        jenkinsFile =  new JenkinsFile(applicationPath, JENKINS_FILE)
        azurePipelinesFile = new AzurePipelinesFile(applicationPath, AZURE_PIPELINES_FILE)
        this.name = pomFile.artifactId

        loadPropertyFiles()
        loadConfigurationFiles()
        loadMuleArtifact()
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

    JenkinsFile getJenkinsfile() {
        return jenkinsFile
    }

    AzurePipelinesFile getAzurePipelinesFile() {
        this.azurePipelinesFile
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
