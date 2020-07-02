package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String CONFIG_FILE_PATH = 'src/main/mule/'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    String name
    GitIgnoreFile gitignoreFile

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
        this.name = pomFile.artifactId

        loadPropertyFiles()
        loadConfigurationFiles()
    }

    void loadPropertyFiles() {
        File resourcePath = new File(applicationPath, 'src/main/resources')
        if (resourcePath.exists()) {
            resourcePath.eachDirRecurse { dir ->
                dir.eachFileMatch(~/.*.properties/) { file ->
                    propertyFiles.add(new PropertyFile(file))
                }
            }
        }
    }

    void loadConfigurationFiles() {
        println('loading configuration files')
        File configurationPath = new File(applicationPath, 'src/main')
        if (!configurationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + configurationPath.absolutePath)
        }
        //TODO if i set the path to src/main/mule and there are no sub-directories, it doesn't load anything.
        configurationPath.eachDirRecurse { dir ->
            dir.eachFileMatch(~/.*.xml/) { file ->
                configurationFiles.add(new ConfigurationFile(file))
            }
        }
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

    ConfigurationFile getConfigurationFile( String fileName ) {
        ConfigurationFile configXML = new ConfigurationFile((new File(applicationPath, CONFIG_FILE_PATH + fileName)))

        configurationFiles.each { configFile->
            if ( configFile.name.equalsIgnoreCase(fileName))
                configXML = configFile
        }
        return configXML
    }

}
