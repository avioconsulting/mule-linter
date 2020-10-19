package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.pom.PomFile

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String README = 'README.md'
    static final String JENKINS_FILE = 'Jenkinsfile'
    static final String PROPERTY_PATH = 'src/main/resources'
    static final String CONFIGURATION_PATH = 'src/main/mule'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    ReadmeFile readmeFile
    JenkinsFile jenkinsFile
    String name
    GitIgnoreFile gitignoreFile
    MuleArtifact muleArtifact

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
        readmeFile = new ReadmeFile(applicationPath, README)
        jenkinsFile =  new JenkinsFile(applicationPath, JENKINS_FILE)
        this.name = pomFile.artifactId

        loadPropertyFiles()
        loadConfigurationFiles()
        loadMuleArtifact()
    }

    void loadPropertyFiles() {
        File resourcePath = new File(applicationPath, PROPERTY_PATH)
        if (resourcePath.exists()) {
            resourcePath.eachDirRecurse { dir ->
                dir.eachFileMatch(~/.*.properties/) { file ->
                    propertyFiles.add(new PropertyFile(file))
                }
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
        muleArtifact = new MuleArtifact(applicationPath)
    }

    File getApplicationPath() {
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
