package com.avioconsulting.mule.linter.model

class ProjectFile {

    File file
    String name

    ProjectFile(File f) {
        file = f
        this.name = f.name
    }

    File getFile() {
        return file
    }

    void setFile(File file) {
        this.file = file
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPath() {
        return file.absolutePath
    }

}
