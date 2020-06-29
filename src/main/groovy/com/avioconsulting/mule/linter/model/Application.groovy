package com.avioconsulting.mule.linter.model

class Application {

    File applicationPath
    Map<String,ProjectFile> files = new HashMap<String,ProjectFile>()

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if( !this.applicationPath.exists()) {
            throw new FileNotFoundException(applicationPath.absolutePath, "Application directory does not exists.")
        }

        //Parse out the files necessary for an application.
        files.put("POM", new File(applicationPath, "pom.xml"))
    }

    File getApplicationPath() {
        return applicationPath
    }

    void setApplicationPath(File applicationPath) {
        this.applicationPath = applicationPath
    }

    PomFile getPomFile() {
        return new PomFile(getFiles().get("POM"))
    }

    Map<String, ProjectFile> getFiles() {
        return files
    }

    void setFiles(Map<String, ProjectFile> files) {
        this.files = files
    }

    Boolean hasFile(String filename) {
        File file = new File(applicationPath, filename)
        if(!file.exists() || !file.canRead() || file.length() == 0) {
            return false
        }

        return true
    }

    Boolean hasGitIgnore() {
        return hasFile('.gitignore')
    }

}
