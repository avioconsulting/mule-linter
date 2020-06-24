package com.avioconsulting.mule.linter.model

class ProjectFile {
    String name
    String type
    File file

    ProjectFile(File f) {
        file = f
    }

    String getType() {
        return type
    }

    void setType(String type) {
        this.type = type
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    File getFile() {
        return file
    }

    void setFile(File file) {
        this.file = file
    }
}
