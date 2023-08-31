package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.Version
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class PomDependencyVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing Maven Dependency'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_DEPENDENCY_POM)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.2'
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.MISSING_DEPENDENCY)
    }

    def 'Correct Equal Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.2'
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Not Greater Than Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)

        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.9'
        rule.versionOperator = 'GREATER_THAN'
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Multiple Greater Than Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)

        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = version
        rule.versionOperator = 'GREATER_THAN'
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '1.3'   | 0
        '1.3.0' | 0
        '1.3.1' | 0
        '1.3.9' | 1
        '1.4'   | 1
    }

    def 'Correct Version in Maven Property for Dependency'() {
        given:
        testApp.addFile(PomFile.POM_XML, DEPENDENCY_PROPERTY_VERSION_POM);
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.2'
        rule.versionOperator = 'EQUAL'
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    private static final String MISSING_DEPENDENCY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.2.1</app.runtime>
        <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>mule-maven-plugin</artifactId>
                <version>${mule.maven.plugin.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <classifier>mule-application</classifier>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <pluginRepositories>
        <pluginRepository>
            <id>mulesoft-releases</id>
            <name>mulesoft release repository</name>
            <layout>default</layout>
            <url>https://repository.mulesoft.org/releases/</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
'''

    private static final String WITH_DEPENDENCY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.2.1</app.runtime>
        <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>mule-maven-plugin</artifactId>
                <version>${mule.maven.plugin.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <classifier>mule-application</classifier>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>1.3.2</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
    <pluginRepositories>
        <pluginRepository>
            <id>mulesoft-releases</id>
            <name>mulesoft release repository</name>
            <layout>default</layout>
            <url>https://repository.mulesoft.org/releases/</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
'''
    private static final String DEPENDENCY_PROPERTY_VERSION_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.2.1</app.runtime>
        <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
        <http.connector.version>1.3.2</http.connector.version>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>mule-maven-plugin</artifactId>
                <version>${mule.maven.plugin.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <classifier>mule-application</classifier>
                </configuration>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>${http.connector.version}</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
    <pluginRepositories>
        <pluginRepository>
            <id>mulesoft-releases</id>
            <name>mulesoft release repository</name>
            <layout>default</layout>
            <url>https://repository.mulesoft.org/releases/</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
'''
}