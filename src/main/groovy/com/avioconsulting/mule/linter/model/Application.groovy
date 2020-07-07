package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'

    File applicationPath
    List<PropertyFile> propertyFiles = []
    List<ConfigurationFile> configurationFiles = []
    PomFile pomFile
    String name

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
        this.name = pomFile.getArtifactId()

        loadPropertyFiles()
        loadConfigurationFiles()
    }

    void loadPropertyFiles() {
        File resourcePath = new File(applicationPath, 'src/main/resources')
        if (!resourcePath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + resourcePath.absolutePath)
        }
        resourcePath.eachDirRecurse { dir ->
            dir.eachFileMatch(~/.*.properties/) { file ->
                propertyFiles.add(new PropertyFile(file))
            }
        }
    }

    void loadConfigurationFiles() {
        File configurationPath = new File(applicationPath, 'src/main/mule')
        if (!configurationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + configurationPath.absolutePath)
        }

        configurationPath.eachFileMatch(~/.*.xml/){ file ->
            configurationFiles.add(new ConfigurationFile(file))
        }
        
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

}
