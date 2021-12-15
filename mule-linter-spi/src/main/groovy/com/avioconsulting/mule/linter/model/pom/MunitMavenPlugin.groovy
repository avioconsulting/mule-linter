package com.avioconsulting.mule.linter.model.pom

import groovy.xml.slurpersupport.GPathResult

/**
 * Example munit-maven-plugin
 *
 *			<plugin>
 *				<groupId>com.mulesoft.munit.tools</groupId>
 *				<artifactId>munit-maven-plugin</artifactId>
 *				<version>${munit.version}</version>
 *				<executions>
 *					<execution>
 *						<id>test</id>
 *						<phase>test</phase>
 *						<goals>
 *							<goal>test</goal>
 *							<goal>coverage-report</goal>
 *						</goals>
 *					</execution>
 *				</executions>
 *				<configuration>
 *					<runtimeProduct>MULE_EE</runtimeProduct>
 *					<runtimeVersion>${app.runtime}</runtimeVersion>
 *					<coverage>
 *						<runCoverage>true</runCoverage>
 *						<failBuild>true</failBuild>
 *						<requiredApplicationCoverage>80</requiredApplicationCoverage>
 *						<requiredResourceCoverage>80</requiredResourceCoverage>
 *						<requiredFlowCoverage>80</requiredFlowCoverage>
 *						<ignoreFiles>
 *							<ignoreFile>nextep-salesforce-sapi.xml</ignoreFile>
 *							<ignoreFile>global-config.xml</ignoreFile>
 *							<ignoreFile>error-handler.xml</ignoreFile>
 *						</ignoreFiles>
 *						<formats>
 *							<format>console</format>
 *							<format>html</format>
 *						</formats>
 *					</coverage>
 *				</configuration>
 *			</plugin>
 */
class MunitMavenPlugin extends PomPlugin {

    static final String GROUP_ID = 'com.mulesoft.munit.tools'
    static final String ARTIFACT_ID = 'munit-maven-plugin'

    MunitMavenPlugin(GPathResult pluginXml, PomFile pomFile) {
        super(pluginXml, pomFile)
    }

    List<String> getIgnoreFiles() {
        List<String> ignoreFiles = []
        pluginXml.configuration.coverage.ignoreFiles.ignoreFile.each {
            ignoreFiles.add(it.text())
        }
        return ignoreFiles
    }

    Integer getIgnoreFilesLineNo() {
        ArtifactDescriptor.getNodeLineNumber(pluginXml.configuration.coverage.ignoreFiles)
    }

}
