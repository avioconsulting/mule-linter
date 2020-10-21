package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
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
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName == PomFile.POM_XML
        violations[0].lineNumber == 0
    }

    def 'Correct Munit Maven Plugin Check'() {
        given:
        testApp.addPom()
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Custom with Defaults Munit Maven Plugin Check'() {
        given:
        testApp.addPom()
        Application app = new Application(testApp.appDir)

        when:
        Map<String, String> coverageAttributes = ['sillyproperty':'incorrect']
        Rule rule = new MunitMavenPluginAttributesRule(coverageAttributes, true)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 52
        violations[0].message.endsWith('sillyproperty|incorrect')
    }

    def 'Check ignoreFiles - Missing one'() {
        given:
        testApp.addPom()
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule(['error-handler.xml', 'global-config.xml', 'something-else.xml'])
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 73
        violations[0].message.endsWith('ignoreFile|something-else.xml')
    }
    def 'Very wrong Munit Maven Plugin'() {
        given:
        testApp.addFile(PomFile.POM_XML, WRONG_PLUGINS_POM)
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitMavenPluginAttributesRule()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 5
        violations[0].fileName == PomFile.POM_XML
        violations[0].rule.ruleName == MunitMavenPluginAttributesRule.RULE_NAME
        violations[0].message.endsWith('runCoverage|true')
        violations[0].lineNumber == 26
        violations[1].message.endsWith('failBuild|true')
        violations[1].lineNumber == 27
        violations[2].message.endsWith('requiredApplicationCoverage|80')
        violations[3].message.endsWith('requiredResourceCoverage|80')
        violations[4].message.endsWith('requiredFlowCoverage|80')
    }

    private static final String MISSING_PLUGINS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
</project>
'''
    private static final String WRONG_PLUGINS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
