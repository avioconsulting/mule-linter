package com.avioconsulting.mule.linter.model

class Application {

    File applicationPath
    List<ProjectFile> files = new ArrayList<ProjectFile>()

    /* Files */
    static final String POM_FILE = "pom.xml"


    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if( !this.applicationPath.exists()) {
            throw new FileNotFoundException(applicationPath.absolutePath, "Application directory does not exists.")
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
        if(!file.exists() || !file.canRead() || file.length() == 0) {
            return false
        }
        return true
    }
}
