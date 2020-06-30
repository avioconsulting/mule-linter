package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists.'
    static final String POM_FILE = 'pom.xml'

    File applicationPath
    List<ProjectFile> files = []

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException(applicationPath.absolutePath, APPLICATION_DOES_NOT_EXIST)
        }
    }

    File getApplicationPath() {
        return applicationPath
    }

    void setApplicationPath(File applicationPath) {
        this.applicationPath = applicationPath
    }

    PomFile getPomFile() {
        return new PomFile(applicationPath, POM_FILE)
    }

    Boolean hasFile(String filename) {
        File file = new File(applicationPath, filename)
        return file.exists() && file.canRead() && file.length() > 0
    }
}
