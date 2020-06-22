package com.avioconsulting.mule.linter.model

class Application {

    File applicationPath

    Application(File applicationPath) {
        this.applicationPath = applicationPath
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
