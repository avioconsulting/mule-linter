package com.avioconsulting.mule.linter.model

class ProjectFile {

    File file
    String name

    ProjectFile(File f) {
        this.file = f
        this.name = f.name
    }

    String getPath() {
        return file.absolutePath
    }

    Boolean doesExist() {
        return file.exists()
    }

}
