package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'

    File applicationPath
    List<ProjectFile> files = []
    PomFile pomFile

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
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

}
