package com.avioconsulting.mule.linter.model

class PomFile extends ProjectFile {

    PomFile(File f) {
        super(f)
        // parse file, to build out pom details
    }

    boolean exists() {
        if(getFile() == null) { return false }
        return getFile().exists()
    }

    String getMunitVersion() {
        return "1.0"
    }
}
