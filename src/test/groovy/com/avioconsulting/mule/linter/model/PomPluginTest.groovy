package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.pom.PomArtifact
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class PomPluginTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
        testApp.addFile(PomFile.POM_XML, PLUGINS_POM)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Find Plugin and check Values'() {
        given:
        Application app = new Application(testApp.appDir)

        when:
        PomArtifact pp = app.pomFile.getPlugin('com.mulesoft.munit.tools', 'munit-maven-plugin')

        then:
        pp.artifactId == 'munit-maven-plugin'
        pp.groupId == 'com.mulesoft.munit.tools'
        pp.version.value == '2.2.1'
        pp.lineNo == 25
        pp.getConfigProperty('requiredResourceCoverage').value == '85'
        pp.getConfigProperty('failBuild').lineNo == 32
    }

    static final String PLUGINS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
\t\txmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
\t\txsi:schemaLocation="http://maven.apache.org/POM/4.0.0
\t\t\thttp://maven.apache.org/maven-v4_0_0.xsd">
\t<modelVersion>4.0.0</modelVersion>
\t<groupId>com.avioconsulting.mulelinter</groupId>
\t<artifactId>sample-mule-app</artifactId>
\t<version>1.0.0</version>
\t<packaging>mule-application</packaging>
\t<name>sample-mule-app-sys-api</name>
\t<properties>
\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
\t\t<app.runtime>4.2.1</app.runtime>
\t\t<mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
\t\t<munit.version>2.2.1</munit.version>
\t</properties>
\t<build>
\t\t<plugins>
\t\t\t<plugin>
\t\t\t\t<groupId>com.avioconsulting.mulelinter</groupId>
\t\t\t\t<artifactId>best-linter-ever</artifactId>
\t\t\t\t<version>${app.runtime}</version>
\t\t\t</plugin>
\t\t\t<plugin>
\t\t\t\t<groupId>com.mulesoft.munit.tools</groupId>
\t\t\t\t<artifactId>munit-maven-plugin</artifactId>
\t\t\t\t<version>${munit.version}</version>
\t\t\t\t<configuration>
\t\t\t\t\t<coverage>
\t\t\t\t\t\t<runCoverage>false</runCoverage>
\t\t\t\t\t\t<failBuild>false</failBuild>
\t\t\t\t\t\t<requiredApplicationCoverage>87</requiredApplicationCoverage>
\t\t\t\t\t\t<requiredResourceCoverage>85</requiredResourceCoverage>
\t\t\t\t\t\t<requiredFlowCoverage>86</requiredFlowCoverage>
\t\t\t\t\t\t<ignoreFiles>
\t\t\t\t\t\t\t<ignoreFile>global-config.xml</ignoreFile>
\t\t\t\t\t\t\t<ignoreFile>error-handler.xml</ignoreFile>
\t\t\t\t\t\t</ignoreFiles>
\t\t\t\t\t</coverage>
\t\t\t\t</configuration>
\t\t\t</plugin>
\t\t</plugins>
\t</build>
</project>'''

}
