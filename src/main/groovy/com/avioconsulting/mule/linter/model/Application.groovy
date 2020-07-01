package com.avioconsulting.mule.linter.model

class Application {

    static final String APPLICATION_DOES_NOT_EXIST = 'Application directory does not exists: '
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'

    File applicationPath
    List<ProjectFile> files = []
    PomFile pomFile
    GitIgnoreFile gitignoreFile

    Application(File applicationPath) {
        this.applicationPath = applicationPath
        if (!this.applicationPath.exists()) {
            throw new FileNotFoundException( APPLICATION_DOES_NOT_EXIST + applicationPath.absolutePath)
        }
        pomFile = new PomFile(applicationPath, POM_FILE)
        gitignoreFile = new GitIgnoreFile(applicationPath, GITIGNORE_FILE)
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

    GitIgnoreFile getGitignoreFile() {
        return gitignoreFile
    }

    Boolean hasFile(String filename) {
        File file = new File(applicationPath, filename)
        return file.exists()
    }

}
