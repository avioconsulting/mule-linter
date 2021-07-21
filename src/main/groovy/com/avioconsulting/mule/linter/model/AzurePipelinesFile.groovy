package com.avioconsulting.mule.linter.model

class AzurePipelinesFile extends ProjectFile {
    public static final String AZURE_PIPELINES = 'azure-pipelines.yml'

    AzurePipelinesFile(File file) {
        super(file)
    }

    AzurePipelinesFile(File application, String fileName) {
        this(new File(application, fileName))
    }
}
