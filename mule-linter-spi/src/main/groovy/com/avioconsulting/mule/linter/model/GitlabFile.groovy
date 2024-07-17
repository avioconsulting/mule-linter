package com.avioconsulting.mule.linter.model

class GitlabFile extends ProjectFile {

    public static final String GITLABFILE = '.gitlab-ci.yml'

    GitlabFile(File file) {
        super(file)
    }

    GitlabFile(File application, String fileName) {
        this(new File(application, fileName))
    }

}
