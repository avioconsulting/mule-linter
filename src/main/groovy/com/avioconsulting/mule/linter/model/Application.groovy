package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.pom.PomFile

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String JENKINS_FILE = 'Jenkinsfile'
    static final String PROPERTY_PATH = 'src/main/resources'
    static final String CONFIGURATION_PATH = 'src/main/mule'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
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

    List<MuleComponent> findComponents(String componentType, String namespace) {
        List<MuleComponent> comps = []
        configurationFiles.each { configFile ->
            List<MuleComponent> comp = configFile.findComponents(componentType, namespace)
            comps += comp
        }
        return comps
    }

    List<MuleComponent> getFlows() {
        return findComponents('flow', ConfigurationFile.MULE_CORE_NAMESPACE)
    }

    List<MuleComponent> getSubFlows() {
        return findComponents('sub-flow', ConfigurationFile.MULE_CORE_NAMESPACE)
    }

    List<MuleComponent> getAllFlows() {
        return (getFlows() + getSubFlows())
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
