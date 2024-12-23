package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.parser.JsonSlurper
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import org.apache.groovy.json.internal.JsonMap
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.MavenInvocationException
import org.yaml.snakeyaml.Yaml

class MuleApplication implements Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'MuleApplication directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String README = 'README.md'
    static final String PROPERTY_PATH = 'src/main/resources'
    static final String CONFIGURATION_PATH = 'src/main/mule'
    static final String MAVEN_HOME_DOES_NOT_EXIST = 'Maven home config does not exists.'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    ReadmeFile readmeFile
    String name
    GitIgnoreFile gitignoreFile
    MuleArtifact muleArtifact

    MuleApplication(File applicationPath, List<String> profiles = null) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        File pFile = new File(applicationPath, POM_FILE)
        // if pom.xml exists in application, get the effective-pom.xml for the application.
        if (pFile.exists())
            pFile = getEffectivePomFile(pFile, profiles)
        pomFile = new PomFile(pFile, pFile.exists() ? new MuleXmlParser().parse(pFile) : null)
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
        readmeFile = new ReadmeFile(applicationPath, README)
        this.name = pomFile.artifactId

        loadPropertyFiles()
        loadConfigurationFiles()
        loadMuleArtifact()
    }

     /**
     * This method generates the effective pom.xml for the application using maven-invoker, and returns effective-pom.xml file.
     * And, the generated effective-pom.xml file will be deleted upon the exit of the application.
     * This method requires Maven home location, which can be passed using below options:
     * 1. Pass maven.home system variable when executing mule-linter
     * 2. Set MAVEN_HOME environment variable in the system executing mule-linter.
     * returns File
     */
    File getEffectivePomFile(File pFile, List<String> profiles = null){
        def mavenHome = null
        // Update mavenHome from system property - maven.home
        if (System.getProperty('maven.home') != null)
            mavenHome = System.getProperty('maven.home')
        else if (System.getenv().get('MAVEN_HOME') != null)
            mavenHome = System.getenv().get('MAVEN_HOME')

        if (mavenHome == null)
            throw new MavenInvocationException( MAVEN_HOME_DOES_NOT_EXIST)

        File effectivePomFile = File.createTempFile("effective-pom", ".xml");
        def mavenInvokeRequest = new DefaultInvocationRequest().with {
            String mvnGoals = 'help:effective-pom -Doutput='+effectivePomFile.getAbsolutePath()
            setGoals([mvnGoals])
            setPomFile(pFile)
            setShowErrors(true)
            setProfiles(profiles)
            it
        }
        def mavenInvoker = new DefaultInvoker()
        mavenInvoker.setMavenHome(new File(mavenHome))
        def result = mavenInvoker.execute(mavenInvokeRequest)
        effectivePomFile.deleteOnExit();
        return effectivePomFile
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
