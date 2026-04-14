package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.pom.ResolvedPlugin
import com.avioconsulting.mule.linter.model.pom.ResolvedDependency
import spock.lang.Specification

/**
 * Tests for Issues 1 and 2:
 * 1. Plugin version from pluginManagement when plugin declared without version
 * 2. Dependency from parent management preserves child-specific details
 */
class PomManagementResolutionTest extends Specification {

    private MuleXmlParser xmlParser = new MuleXmlParser()

    private PomFile createPomFile(String path) {
        File file = new File(path)
        def pomXml = xmlParser.parse(file)
        return new PomFile(file, pomXml)
    }

    def "Plugin version is resolved from local pluginManagement when plugin declared without version"() {
        given: "A POM with plugin in build/plugins but no version, and version in pluginManagement"
        def pomFile = createPomFile("src/test/resources/PomManagementTest/child-with-plugin-management/pom.xml")

        when: "Resolving the plugin"
        ResolvedPlugin result = pomFile.resolvePlugin("org.apache.maven.plugins", "maven-compiler-plugin")

        then: "Plugin is found with version from management"
        result != null
        result.version == "3.8.1"
        result.isVersionFromManagement == true
        result.versionSource == pomFile
        result.parentDepth == 0
        result.plugin != null
        result.plugin.artifactId == "maven-compiler-plugin"
    }

    def "Plugin with explicit version uses local version (local wins over management)"() {
        given: "A POM with plugin that has explicit version in build/plugins"
        def pomFile = createPomFile("src/test/resources/PomManagementTest/local-version-wins/pom.xml")

        when: "Resolving the plugin"
        ResolvedPlugin result = pomFile.resolvePlugin("org.apache.maven.plugins", "maven-compiler-plugin")

        then: "Local version wins over management version"
        result != null
        result.version == "3.10.0"
        result.isVersionFromManagement == false
        result.versionSource == pomFile
    }

    def "Dependency from parent management preserves child-specific scope"() {
        given: "A child POM with dependency from parent's dependencyManagement"
        def childPom = createPomFile("src/test/resources/PomManagementTest/child-with-parent-mgmt/pom.xml")

        when: "Resolving the dependency"
        ResolvedDependency result = childPom.resolveDependency("com.example", "my-lib")

        then: "Dependency is found with version from parent management"
        result != null
        result.version == "2.0.0"
        result.isVersionFromManagement == true
        result.parentDepth == 1
        
        and: "Child-specific details (scope) are preserved"
        result.dependency != null
        result.dependency.scope?.value == "runtime"
        result.dependency.artifactId == "my-lib"
    }

    def "Dependency with explicit version uses local version (local wins over management)"() {
        given: "A POM with dependency that has explicit version, different from parent management"
        def pomFile = createPomFile("src/test/resources/PomManagementTest/dep-local-version-wins/pom.xml")

        when: "Resolving the dependency"
        ResolvedDependency result = pomFile.resolveDependency("com.example", "my-lib")

        then: "Local version wins over management version"
        result != null
        result.version == "3.0.0"
        result.isVersionFromManagement == false
        result.versionSource == pomFile
        result.dependency.scope?.value == "compile"
    }
}
