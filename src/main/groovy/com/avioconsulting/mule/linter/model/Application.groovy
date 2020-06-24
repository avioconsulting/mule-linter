package com.avioconsulting.mule.linter.model

class Application {

    File applicationPath
//    ProjectFile[] files
    Map<String,ProjectFile> files = new HashMap<String,ProjectFile>()

    Application(File applicationPath) {
        this.applicationPath = applicationPath

        files.put("POM", new File(applicationPath, "pom.xml"))

        // add logic to put in required files into files map
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


// These to go away....
    // implement getFile("name")

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
