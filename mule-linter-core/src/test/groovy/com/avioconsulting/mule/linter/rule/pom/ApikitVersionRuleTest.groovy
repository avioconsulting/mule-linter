package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class ApikitVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing APIKit Maven Dependency'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_DEPENDENCY_POM)
        Rule rule = new ApikitVersionRule()
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.MISSING_DEPENDENCY)
    }

    def 'APIKit Maven Dependency version does not match'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)

        when:
        Rule rule = new ApikitVersionRule()
        rule.artifactVersion = '1.9.2'
        rule.init()
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Correct Equal Maven Dependency version for APIKit module'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)
        Rule rule = new ApikitVersionRule()
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Correct Version in Maven Property for APIKit Dependency'() {
        given:
        testApp.addFile(PomFile.POM_XML, DEPENDENCY_PROPERTY_VERSION_POM);
        Rule rule = new ApikitVersionRule()
        rule.artifactVersion = '1.8.0'
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
        <app.runtime>4.2.2</app.runtime>
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
        <app.runtime>4.2.2</app.runtime>
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
            <groupId>org.mule.modules</groupId>
            <artifactId>mule-apikit-module</artifactId>
            <version>1.9.1</version>
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
        <app.runtime>4.2.2</app.runtime>
        <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
        <apikit-version>1.9.1</apikit-version>
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
            <groupId>org.mule.modules</groupId>
            <artifactId>mule-apikit-module</artifactId>
            <version>${apikit-version}</version>
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