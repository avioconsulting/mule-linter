package com.avioconsulting.mule.linter.model

class JenkinsFile extends ProjectFile {

    public static final String JENKINSFILE = 'Jenkinsfile'

    JenkinsFile(File file) {
        super(file)
    }

    JenkinsFile(File application, String fileName) {
        this(new File(application, fileName))
    }

}
