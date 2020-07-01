package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'

    File applicationPath
    List<ProjectFile> files = []
    PomFile pomFile
    String name

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
        this.name = applicationPath.name
    }

    File getApplicationPath() {
        return applicationPath
    }

    void setApplicationPath(File applicationPath) {
        this.applicationPath = applicationPath
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

    void setName(String name) {
        this.name = name
    }

    List<ProjectFile> getPropertyFiles() {
        List<ProjectFile> files = []
        applicationPath.eachDirRecurse { dir ->
            dir.eachFileMatch(~/.*.properties/) { file ->
                files.add(new ProjectFile(file))
            }
        }
        return files
    }

}
