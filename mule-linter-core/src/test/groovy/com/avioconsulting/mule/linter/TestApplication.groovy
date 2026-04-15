package com.avioconsulting.mule.linter


import com.avioconsulting.mule.linter.model.MuleArtifact
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.GitIgnoreFile
import com.avioconsulting.mule.linter.rule.cicd.AzurePipelinesExistsRule
import com.avioconsulting.mule.linter.rule.cicd.GitlabFileExistsRule
import com.avioconsulting.mule.linter.rule.cicd.JenkinsFileExistsRule

@SuppressWarnings(['StaticFieldsBeforeInstanceFields', 'BuilderMethodWithSideEffects', 'FactoryMethodName'])
class TestApplication {

    static final String SAMPLE_APP_NAME = 'SampleMuleApp'
    
    // Store previous value of system property for restoration
    private String previousSkipEffectivePomValue
    static final List<String> CONFIGS = ['src/main/mule/business-logic.xml',
                                         'src/main/mule/global-config.xml',
                                         'src/main/mule/sample-mule-app-api.xml']
    static final String PROPERTY_FILE_DIR = 'src/main/resources/properties/'
    static final List<String> DIRECTORY_STRUCTURE = ['src/main/mule', 'src/main/resources/properties']

    File appDir
    private final File sampleAppDir = new File(this.class.classLoader.getResource(SAMPLE_APP_NAME).file)

    TestApplication() {
    }

    void initialize() {
        appDir = File.createTempDir()
        buildDirectoryStructure()
        println 'Created temporary app: ' + appDir.path
    }

    void addPom() {
        copyFileFromBaseApp(PomFile.POM_XML)
        // Create a simplified pom.xml for tests to avoid slow effective-pom generation
        // The real MuleApplication constructor will use effective-pom which tries to download
        // dependencies from MuleSoft repositories, causing tests to hang
    }
    
    /**
     * Create comprehensive parent-child POM structure for testing
     * effective POM resolution with parent inheritance
     */
    void addComprehensiveParentSample() {
        // Copy parent POM to parent/ subdirectory
        File parentDir = new File(appDir, 'parent')
        parentDir.mkdirs()
        copyFileFromResource('ComprehensiveParentSample/parent/pom.xml', parentDir)
        
        // Copy child POM to app root
        copyFileFromResource('ComprehensiveParentSample/child/pom.xml', appDir)
    }
    
    /**
     * Enable effective POM generation for tests.
     * Clears the skip flag so MuleApplication uses effective POM.
     * Remember to call cleanup() in test cleanup to restore the previous value.
     */
    void useEffectivePomGeneration() {
        previousSkipEffectivePomValue = System.getProperty('mule.linter.skipEffectivePom')
        System.clearProperty('mule.linter.skipEffectivePom')
    }
    
    /**
     * Create a minimal POM for testing specific scenarios
     */
    void addMinimalPom(String pomContent) {
        File pomFile = new File(appDir, PomFile.POM_XML)
        pomFile.text = pomContent
    }
    
    private void copyFileFromResource(String resourcePath, File targetDir) {
        def resource = this.class.classLoader.getResource(resourcePath)
        if (resource == null) {
            throw new FileNotFoundException("Resource not found: $resourcePath")
        }
        File resourceFile = new File(resource.file)
        new File(targetDir, resourceFile.name) << resourceFile.text
    }

    void addGitIgnore() {
        File gitIgnore = new File(appDir, GitIgnoreFile.GITIGNORE)
        writeFile(gitIgnore, GITIGNORE_CONTENTS)
    }

    void addAzurePipelinesFile(String yamlContents = null) {
        File azurePipelinesFile = new File(appDir, AzurePipelinesExistsRule.AZURE_PIPELINES)
        yamlContents = yamlContents ?: "pool:\n" +
                "  vmImage: 'ubuntu-latest'"
        writeFile(azurePipelinesFile, yamlContents)
    }

    void addJenkinsfile() {
        File jenkinsFile = new File(appDir, JenkinsFileExistsRule.JENKINSFILE)
        writeFile(jenkinsFile, '')
    }

    void addGitlabFile() {
        File gitlabFile = new File(appDir, GitlabFileExistsRule.GITLABFILE)
        writeFile(gitlabFile, '')
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
    
    /**
     * Restore system properties that were modified during test setup.
     * Call this in test cleanup() to avoid polluting other tests.
     */
    void cleanup() {
        // Restore the mule.linter.skipEffectivePom property
        if (previousSkipEffectivePomValue != null) {
            System.setProperty('mule.linter.skipEffectivePom', previousSkipEffectivePomValue)
        } else {
            System.clearProperty('mule.linter.skipEffectivePom')
        }
    }

    void removeFile(String fileName) {
        File fileToRemove = new File(appDir, fileName)
        if (fileToRemove.exists()) {
            fileToRemove.delete()
        }
    }

    @SuppressWarnings('UnnecessaryGetter')
    void cleanDirectory(String directory) {
        File directoryPath = new File(appDir, directory)
        directoryPath.listFiles().each {
            if (!it.isDirectory()) {
                it.delete()
            }
        }
    }

    void buildConfigContent(String filename, String content) {
        addFile("src/main/mule/$filename", MULE_CONFIG_START + content + MULE_CONFIG_END)
    }

    void buildPropertyContent(String filename, String content) {
        addFile("src/main/resources/properties/$filename", content)
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
