package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.pom.PomFile
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class MunitMavenPluginAttributesRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing Munit Maven Plugin Check'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_PLUGINS_POM)
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'Missing munit-maven-plugin'
        violations[0].lineNumber == 0
    }

    def 'Correct Munit Maven Plugin Check'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Custom with Defaults Munit Maven Plugin Check'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Map<String, String> coverageAttributes = ['sillyproperty':'incorrect']
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.setCoverageAttributeMap(coverageAttributes)
        rule.setIncludeDefaults(true)
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.endsWith('sillyproperty|incorrect')
        violations[0].message.startsWith(MunitMavenPluginAttributesRule.RULE_MESSAGE)
    }

    def 'Check ignoreFiles - Missing one'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.ignoreFiles = ['error-handler.xml', 'global-config.xml', 'something-else.xml']
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.endsWith('ignoreFile|something-else.xml')
        violations[0].message.startsWith(MunitMavenPluginAttributesRule.RULE_MESSAGE_MISSING)
    }
    def 'Very wrong Munit Maven Plugin'() {
        given:
        testApp.addFile(PomFile.POM_XML, WRONG_PLUGINS_POM)
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 5
        violations[0].fileName == PomFile.POM_XML
        violations[0].rule.ruleName == MunitMavenPluginAttributesRule.RULE_NAME
        violations[0].message.endsWith('runCoverage|true')
        violations[1].message.endsWith('failBuild|true')
        violations[2].message.endsWith('requiredApplicationCoverage|80')
        violations[3].message.endsWith('requiredResourceCoverage|80')
        violations[4].message.endsWith('requiredFlowCoverage|80')
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

    private static final String WRONG_PLUGINS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
        <munit.version>2.2.1</munit.version>
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
                        <runCoverage>false</runCoverage>
                        <failBuild>false</failBuild>
                        <requiredApplicationCoverage>87</requiredApplicationCoverage>
                        <requiredResourceCoverage>85</requiredResourceCoverage>
                        <requiredFlowCoverage>86</requiredFlowCoverage>
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

}
