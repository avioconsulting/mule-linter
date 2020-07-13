package com.avioconsulting.mule.linter

import com.avioconsulting.mule.linter.model.JenkinsFile
import com.avioconsulting.mule.linter.model.MuleArtifact
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.GitIgnoreFile

@SuppressWarnings('StaticFieldsBeforeInstanceFields')
class TestApplication {

    static final String SAMPLE_APP_NAME = 'SampleMuleApp'
    static final List<String> CONFIGS = ['src/main/mule/business-logic.xml',
                                         'src/main/mule/global-config.xml',
                                         'src/main/mule/sample-mule-app-api.xml']
    static final String PROPERTY_FILE_DIR = 'src/main/resources/properties/'
    static final List<String> DIRECTORY_STRUCTURE = ['src/main/mule', 'src/main/resources/properties']

    File appDir
    private final File sampleAppDir = new File(this.class.classLoader.getResource(SAMPLE_APP_NAME).file)

    TestApplication() {
    }

    void create() {
        appDir = File.createTempDir()
        buildDirectoryStructure()
        println 'Created temporary app: ' + appDir.path
    }

    void addPom() {
        copyFileFromBaseApp(PomFile.POM_XML)
    }

    void addGitIgnore() {
        File gitIgnore = new File(appDir, GitIgnoreFile.GITIGNORE)
        writeFile(gitIgnore, GITIGNORE_CONTENTS)
    }

    void addJenkinsfile() {
        File jenkinsFile = new File(appDir, JenkinsFile.JENKINSFILE)
        writeFile(jenkinsFile, '')
    }

    void addMuleArtifact() {
        copyFileFromBaseApp(MuleArtifact.MULE_ARTIFACT_JSON)
    }

    void addConfig() {
        File muleDir = new File(appDir, '/src/main/mule')
        if (!muleDir.exists()) {
            muleDir.mkdirs()
        }
        CONFIGS.each { config ->
            copyFileFromBaseApp(config)
        }
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

    void buildConfigContent(String filename, String content) {
        addFile("src/main/mule/$filename", MULE_CONFIG_START + content + MULE_CONFIG_END)
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

    private static final String MULE_CONFIG_START = '''<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns:ee="http://www.mulesoft.org/schema/mule/ee/core" xmlns="http://www.mulesoft.org/schema/mule/core"
\txmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
\t\thttp://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd">
'''
    private static final String MULE_CONFIG_END = '''
</mule>'''
}
