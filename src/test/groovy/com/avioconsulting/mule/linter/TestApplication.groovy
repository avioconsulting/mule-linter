package com.avioconsulting.mule.linter

import com.avioconsulting.mule.linter.model.MuleArtifact
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.GitIgnoreFile

@SuppressWarnings('StaticFieldsBeforeInstanceFields')
class TestApplication {

    static final String SAMPLE_APP_NAME = 'SampleMuleApp'
    static final String CONFIG_SIMPLE = 'src/main/mule/simple-flow.xml'
    static final String PROPERTY_FILE_DIR = 'src/main/resources/properties/'
    static final List<String> DIRECTORY_STRUCTURE = ['src/main/mule', 'src/main/resources/properties']

    File appDir
    private final File sampleAppDir = new File(this.class.classLoader.getResource(SAMPLE_APP_NAME).file)

    TestApplication() {
    }

    void create() {
        appDir = File.createTempDir()
        buildDirectoryStructure()
    }

    void addPom() {
        copyFileFromBaseApp(PomFile.POM_XML)
    }

    void addGitIgnore() {
        File gitIgnore = new File(appDir, GitIgnoreFile.GITIGNORE)
        writeFile(gitIgnore, GITIGNORE_CONTENTS)
    }

    void addMuleArtifact() {
        copyFileFromBaseApp(MuleArtifact.MULE_ARTIFACT_JSON)
    }

    void addConfig() {
        File muleDir = new File(appDir, '/src/main/mule')
        if (!muleDir.exists()) {
            muleDir.mkdirs()
        }
        copyFileFromBaseApp(CONFIG_SIMPLE)
    }

    void addPropertyFiles() {
        File propDir = new File(appDir, PROPERTY_FILE_DIR)
        if (!propDir.exists()) {
            propDir.mkdirs()
        }
        File samplePropDir = new File(sampleAppDir, PROPERTY_FILE_DIR)
        samplePropDir.listFiles().each { file ->
            copyFileFromBaseApp(PROPERTY_FILE_DIR + file.name)
        }
    }

    void addPropertyFiles(List<String> propertyFileNames) {
        File propDir = new File(appDir, PROPERTY_FILE_DIR)
        if (!propDir.exists()) {
            propDir.mkdirs()
        }
        propertyFileNames.each { fileName ->
            copyFileFromBaseApp(PROPERTY_FILE_DIR + fileName)
        }
    }
    void addFile(String fileName, String contents) {
        writeFile(new File(appDir, fileName), contents)
    }

    void remove() {
        appDir.deleteDir()
    }

    void removeFile(String fileName) {
        File fileToRemove = new File(appDir, fileName)
        if (fileToRemove.exists()) {
            fileToRemove.delete()
        }
    }

    private void buildDirectoryStructure() {
        DIRECTORY_STRUCTURE.each { dir ->
            File directory = new File(appDir, dir)
            directory.mkdirs()
        }
    }

    private void copyFileFromBaseApp(String fileName) {
        new File(appDir, fileName) << new File(sampleAppDir, fileName).text
    }

    private void writeFile (File toWrite, String contents) {
        toWrite.withPrintWriter { pw ->
            pw.print(contents)
        }
    }

    private static final String GITIGNORE_CONTENTS = '''# Compiled class file
*.class

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Project Files #
.idea/
.gradle/

build/
out/'''
}
