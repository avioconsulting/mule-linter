package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

/**
 * Consolidated test class for all POM version-related rules.
 * Tests Mule Maven Plugin, MUnit, Mule Runtime, APIKit, and generic dependency version rules.
 * All tests use the comprehensive parent-child sample structure for testing effective POM resolution.
 */
@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class PomVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    // ==================== MULE MAVEN PLUGIN VERSION TESTS ====================

    def 'Mule Maven Plugin version matches expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MuleMavenPluginVersionRule()
        rule.version = '4.6.0'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Mule Maven Plugin version does not match'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MuleMavenPluginVersionRule()
        rule.version = '3.3.5'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    // ==================== MUNIT VERSION TESTS ====================

    def 'MUnit version property matches expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule()
        rule.version = '3.6.3'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'MUnit version property does not match'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule()
        rule.version = '3.2.1'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith('munit.version maven property value does not match expected value')
    }

    def 'MUnit version property is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_MUNIT)
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule()
        rule.version = '3.2.1'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith('munit.version does not exist')
    }

    // ==================== MUNIT PLUGIN VERSION TESTS ====================

    def 'MUnit Maven Plugin version matches expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitPluginVersionRule()
        rule.version = '3.6.3'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'MUnit Maven Plugin version does not match'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitPluginVersionRule()
        rule.version = '3.2.1'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].rule.ruleId == MunitPluginVersionRule.RULE_ID
    }

    def 'MUnit Maven Plugin is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_MUNIT_PLUGIN)
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitPluginVersionRule()
        rule.version = '3.2.1'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].rule.ruleId == MunitPluginVersionRule.RULE_ID
        violations[0].lineNumber == 0
        violations[0].message.startsWith(PomPluginAttributeRule.MISSING_PLUGIN)
    }

    // ==================== MULE RUNTIME VERSION TESTS ====================

    def 'Mule Runtime version matches expected'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MuleRuntimeVersionRule()
        rule.version = '4.9.16'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Mule Runtime version does not match'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MuleRuntimeVersionRule()
        rule.version = '4.9.15'
        rule.init()
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
    }

    // ==================== APIKIT VERSION TESTS ====================

    def 'APIKit dependency version matches expected'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_APIKIT)
        Rule rule = new ApikitVersionRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'APIKit dependency version does not match'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_APIKIT)
        Rule rule = new ApikitVersionRule()
        rule.artifactVersion = '1.9.2'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.RULE_VIOLATION_MESSAGE)
    }

    def 'APIKit dependency is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_APIKIT)
        Rule rule = new ApikitVersionRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.MISSING_DEPENDENCY)
    }

    def 'APIKit version via property matches expected'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_APIKIT_PROPERTY)
        Rule rule = new ApikitVersionRule()
        rule.artifactVersion = '1.8.0'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    // ==================== GENERIC DEPENDENCY VERSION TESTS ====================

    def 'Generic dependency version matches with EQUAL operator'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_HTTP_CONNECTOR)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.10.3'
        rule.versionOperator = 'EQUAL'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Generic dependency version satisfies GREATER_THAN operator'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_HTTP_CONNECTOR)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.10.0'
        rule.versionOperator = 'GREATER_THAN'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Generic dependency version fails GREATER_THAN operator'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_HTTP_CONNECTOR)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.10.9'
        rule.versionOperator = 'GREATER_THAN'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Generic dependency version via property matches'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_HTTP_CONNECTOR_PROPERTY)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.2'
        rule.versionOperator = 'EQUAL'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Generic dependency is missing'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITHOUT_DEPENDENCIES)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.3.2'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].message.startsWith(PomDependencyVersionRule.MISSING_DEPENDENCY)
        violations[0].fileName == PomFile.POM_XML
    }

    // ==================== MULTIPLE VERSION OPERATOR TESTS ====================

    def 'Multiple GREATER_THAN version checks'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_HTTP_CONNECTOR)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = version
        rule.versionOperator = 'GREATER_THAN'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version     | size
        '1.10'      | 0
        '1.10.0'    | 0
        '1.10.2'    | 0
        '1.10.9'    | 1
        '1.11'      | 1
    }

    // ==================== TEST DATA ====================

    private static final String POM_WITHOUT_MUNIT = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

    private static final String POM_WITHOUT_MUNIT_PLUGIN = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

    private static final String POM_WITH_APIKIT = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
    <dependencies>
        <dependency>
            <groupId>org.mule.modules</groupId>
            <artifactId>mule-apikit-module</artifactId>
            <version>1.9.1</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
</project>
'''

    private static final String POM_WITHOUT_APIKIT = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

    private static final String POM_WITH_APIKIT_PROPERTY = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
</project>
'''

    private static final String POM_WITH_HTTP_CONNECTOR = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>1.10.3</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
</project>
'''

    private static final String POM_WITH_HTTP_CONNECTOR_PROPERTY = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
</project>
'''

    private static final String POM_WITHOUT_DEPENDENCIES = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
}
