package com.avioconsulting.mule.linter

@SuppressWarnings('StaticFieldsBeforeInstanceFields')
class TestApplication {

    static final String SAMPLE_APP_NAME = 'SampleMuleApp'
    static final String POM_FILE = 'pom.xml'
    static final String GITIGNORE_FILE = '.gitignore'
    static final String CONFIG_SIMPLE = 'src/main/mule/simple-logging-flow.xml'

    File appDir
    File gitIgnore
    File sampleAppDir

    TestApplication() {
        sampleAppDir = new File(this.class.classLoader.getResource(SAMPLE_APP_NAME).file)
        appDir = File.createTempDir()
    }

    TestApplication addPom() {
        copyFileFromBaseApp(POM_FILE)
        return this
    }

    void addGitIgnore() {
        File gitIgnore = new File(appDir, GITIGNORE_FILE)
        writeFile(gitIgnore, GITIGNORE_CONTENTS)
    }
    void addConfig() {
        copyFileFromBaseApp(CONFIG_SIMPLE)
    }

    void copyFileFromBaseApp(String fileName) {
        new File(appDir, fileName) << new File(sampleAppDir, fileName).text
    }

    void addFile(String fileName, String contents) {
        writeFile(new File(appDir, fileName), contents)
    }

    void remove() {
        appDir.deleteDir()
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
