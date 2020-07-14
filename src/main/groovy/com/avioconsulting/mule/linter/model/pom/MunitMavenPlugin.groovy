package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult

class MunitMavenPlugin extends PomPlugin {

    static final String GROUP_ID = 'com.mulesoft.munit.tools'
    static final String ARTIFACT_ID = 'munit-maven-plugin'

    MunitMavenPlugin(GPathResult pluginXml, PomFile pomFile) {
        super(pluginXml, pomFile)
    }

    @SuppressWarnings('UnnecessaryCollectCall')
    Map<String, String> getConfigurationCoverageDetails() {
        Map<String, String> m = [:]
        pluginXml.configuration.coverage.childNodes().each {
            String value = (it.childNodes().size() > 0) ? it.childNodes().collect { it.text() }.join(',') : it.text()
            m.put(it.name as String, value)
        }
        return m
    }

    String getCoverageValue(String property) {
        pluginXml.configuration.coverage.find { it.name() == property }?.text()
    }

    List<String> getIgnoreFiles() {
        List<String> ignoreFiles = []
        pluginXml.configuration.coverage.ignoreFiles.ignoreFile.each {
            ignoreFiles.add(it.text())
        }
        return ignoreFiles
    }

    Integer getIgnoreFilesLineNo() {
        MuleXmlParser.getNodeLineNumber(pluginXml.configuration.coverage.ignoreFiles)
    }

}

/*
			<plugin>
				<groupId>com.mulesoft.munit.tools</groupId>
				<artifactId>munit-maven-plugin</artifactId>
				<version>${munit.version}</version>
				<executions>
					<execution>
						<id>test</id>
						<phase>test</phase>
						<goals>
							<goal>test</goal>
							<goal>coverage-report</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<runtimeProduct>MULE_EE</runtimeProduct>
					<runtimeVersion>${app.runtime}</runtimeVersion>
					<coverage>
						<runCoverage>true</runCoverage>
						<failBuild>true</failBuild>
						<requiredApplicationCoverage>80</requiredApplicationCoverage>
						<requiredResourceCoverage>80</requiredResourceCoverage>
						<requiredFlowCoverage>80</requiredFlowCoverage>
						<ignoreFiles>
							<ignoreFile>nextep-salesforce-sapi.xml</ignoreFile>
							<ignoreFile>global-config.xml</ignoreFile>
							<ignoreFile>error-handler.xml</ignoreFile>
						</ignoreFiles>
						<formats>
							<format>console</format>
							<format>html</format>
						</formats>
					</coverage>
				</configuration>
			</plugin>
 */
