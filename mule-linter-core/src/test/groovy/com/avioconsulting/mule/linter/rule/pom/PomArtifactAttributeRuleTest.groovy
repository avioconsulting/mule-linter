package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class PomArtifactAttributeRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing Maven Plugin'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_PLUGINS_POM)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'com.mulesoft.munit.tools'
        rule.artifactId = 'munit-maven-plugin'
        rule.attributes = ['version':'2.2.1']
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomPluginAttributeRule.MISSING_PLUGIN)
    }

    def 'Correct Maven Plugin version'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_POM)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'com.mulesoft.munit.tools'
        rule.artifactId = 'munit-maven-plugin'
        rule.attributes = ['version':'2.2.1']
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Maven Plugin version'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_POM)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version':'3.3.6']
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomPluginAttributeRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Correct Maven Plugin version check as property'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_WITHPROPERTY_POM)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version':'3.3.5']
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Maven Plugin version check as property'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_WITHPROPERTY_POM)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version':'3.3.6']
        rule.init()

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomPluginAttributeRule.RULE_VIOLATION_MESSAGE)
    }

    private static final String MISSING_PLUGINS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

    private static final String PLUGINS_EXISTS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
        <munit.version>2.2.1</munit.version>
        <munit.failBuild>true</munit.failBuild>
        <munit.requiredApplicationCoverage>80</munit.requiredApplicationCoverage>
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
                    <coverage>
                        <runCoverage>true</runCoverage>
                        <failBuild>${munit.failBuild}</failBuild>
                        <requiredApplicationCoverage>${munit.requiredApplicationCoverage}</requiredApplicationCoverage>
                        <requiredResourceCoverage>80</requiredResourceCoverage>
                        <requiredFlowCoverage>80</requiredFlowCoverage>
                        <ignoreFiles>
                            <ignoreFile>global-config.xml</ignoreFile>
                            <ignoreFile>error-handler.xml</ignoreFile>
                        </ignoreFiles>
                    </coverage>
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

    private static final String PLUGINS_EXISTS_WITHPROPERTY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

}

