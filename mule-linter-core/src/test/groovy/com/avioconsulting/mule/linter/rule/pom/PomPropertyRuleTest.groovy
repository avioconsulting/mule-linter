package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

/**
 * Consolidated test class for all POM property-related rules.
 * Tests property value validation, plugin attributes, and property override scenarios.
 * All tests use the comprehensive parent-child sample structure for testing effective POM resolution.
 */
@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class PomPropertyRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    // ==================== POM PROPERTY VALUE TESTS ====================

    def 'Property value matches expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'munit.version'
        rule.propertyValue = '3.6.3'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Property value does not match'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'munit.version'
        rule.propertyValue = '3.2.1'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'munit.version maven property value does not match expected value. Expected: 3.2.1 found: 3.6.3'
        violations[0].fileName == PomFile.POM_XML
    }

    def 'Property does not exist'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'invalid.property'
        rule.propertyValue = 'some-value'
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == 'invalid.property does not exist in <properties></properties>'
        violations[0].fileName == PomFile.POM_XML
    }

    def 'Multiple property values match expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule1 = new PomPropertyValueRule()
        rule1.propertyName = 'app.runtime'
        rule1.propertyValue = '4.9.16'

        Rule rule2 = new PomPropertyValueRule()
        rule2.propertyName = 'mule.maven.plugin.version'
        rule2.propertyValue = '4.6.0'

        List<RuleViolation> violations = []
        violations.addAll(rule1.execute(app))
        violations.addAll(rule2.execute(app))

        then:
        violations.size() == 0
    }

    // ==================== PLUGIN ATTRIBUTE TESTS ====================

    def 'Plugin attribute matches expected'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_MUNIT_PLUGIN)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'com.mulesoft.munit.tools'
        rule.artifactId = 'munit-maven-plugin'
        rule.attributes = ['version': '2.2.1']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Plugin attribute does not match'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_MUNIT_PLUGIN)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '3.3.6']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomPluginAttributeRule.RULE_VIOLATION_MESSAGE)
        violations[0].fileName == PomFile.POM_XML
    }

    def 'Plugin attribute matches via property'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_PLUGIN_PROPERTY)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '3.3.5']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Plugin is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_MUNIT_PLUGIN)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'com.mulesoft.munit.tools'
        rule.artifactId = 'munit-maven-plugin'
        rule.attributes = ['version': '2.2.1']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomPluginAttributeRule.MISSING_PLUGIN)
        violations[0].fileName == PomFile.POM_XML
    }

    // ==================== MUNIT MAVEN PLUGIN ATTRIBUTES TESTS ====================

    def 'MUnit Maven Plugin attributes match defaults'() {
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

    def 'MUnit Maven Plugin is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_MUNIT_PLUGIN)
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

    def 'MUnit Maven Plugin with custom attribute fails'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Map<String, String> coverageAttributes = ['sillyproperty': 'incorrect']
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

    def 'MUnit Maven Plugin ignoreFiles check'() {
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

    def 'MUnit Maven Plugin with wrong attributes'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_WRONG_MUNIT_PLUGIN)
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

    // ==================== POM EXISTS TESTS ====================

    def 'POM file exists'() {
        given:
        testApp.addPom()
        Rule rule = new PomExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'POM file does not exist'() {
        given:
        Rule rule = new PomExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message == PomExistsRule.FILE_NOT_EXISTS
    }

    // ==================== POM ARTIFACT/PLUGIN ATTRIBUTE TESTS ====================

    def 'POM plugin attribute matches expected'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_PLUGIN_PROPERTY)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '3.3.5']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'POM plugin attribute does not match'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_PLUGIN_PROPERTY)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '3.3.6']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    // ==================== TEST DATA ====================

    private static final String POM_WITH_MUNIT_PLUGIN = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
</project>
'''

    private static final String POM_WITHOUT_MUNIT_PLUGIN = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
            </plugin>
        </plugins>
    </build>
</project>
'''

    private static final String POM_WITH_PLUGIN_PROPERTY = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
            </plugin>
        </plugins>
    </build>
</project>
'''

    private static final String POM_WITH_WRONG_MUNIT_PLUGIN = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
</project>
'''
}
