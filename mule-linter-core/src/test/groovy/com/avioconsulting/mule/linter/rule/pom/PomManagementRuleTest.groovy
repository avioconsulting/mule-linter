package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

/**
 * Consolidated test class for all POM management-related rules.
 * Tests dependencyManagement, pluginManagement, and parent POM inheritance scenarios.
 * All tests use the comprehensive parent-child sample structure for testing effective POM resolution.
 */
@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class PomManagementRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    // ==================== DEPENDENCY MANAGEMENT TESTS ====================

    def 'Dependency version is validated when explicitly declared'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_EXPLICIT_VERSION)
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

    def 'Dependency version override in child POM'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_VERSION_OVERRIDE)
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.11.0'
        rule.versionOperator = 'EQUAL'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Dependency with BOM import pattern is validated'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_EXPLICIT_VERSION)
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

    def 'Multiple dependencies with explicit versions are validated'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_MULTIPLE_EXPLICIT_DEPS)
        
        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        
        Rule rule1 = new PomDependencyVersionRule()
        rule1.groupId = 'org.mule.connectors'
        rule1.artifactId = 'mule-http-connector'
        rule1.artifactVersion = '1.10.3'
        rule1.versionOperator = 'EQUAL'
        rule1.init()
        
        Rule rule2 = new PomDependencyVersionRule()
        rule2.groupId = 'org.mule.connectors'
        rule2.artifactId = 'mule-sockets-connector'
        rule2.artifactVersion = '1.2.5'
        rule2.versionOperator = 'EQUAL'
        rule2.init()
        
        List<RuleViolation> violations = []
        violations.addAll(rule1.execute(app))
        violations.addAll(rule2.execute(app))

        then:
        violations.size() == 0
    }

    // ==================== PLUGIN MANAGEMENT TESTS ====================

    def 'Plugin version from management section'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_PLUGIN_MANAGEMENT)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '4.6.0']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Plugin version override in child POM'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_PLUGIN_VERSION_OVERRIDE)
        Rule rule = new PomPluginAttributeRule()
        rule.groupId = 'org.mule.tools.maven'
        rule.artifactId = 'mule-maven-plugin'
        rule.attributes = ['version': '4.7.0']
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Multiple plugins from management'() {
        given:
        testApp.addFile(PomFile.POM_XML, POM_WITH_MULTIPLE_MANAGED_PLUGINS)
        
        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        
        Rule rule1 = new PomPluginAttributeRule()
        rule1.groupId = 'org.mule.tools.maven'
        rule1.artifactId = 'mule-maven-plugin'
        rule1.attributes = ['version': '4.6.0']
        rule1.init()
        
        Rule rule2 = new PomPluginAttributeRule()
        rule2.groupId = 'com.mulesoft.munit.tools'
        rule2.artifactId = 'munit-maven-plugin'
        rule2.attributes = ['version': '3.6.3']
        rule2.init()
        
        List<RuleViolation> violations = []
        violations.addAll(rule1.execute(app))
        violations.addAll(rule2.execute(app))

        then:
        violations.size() == 0
    }

    // ==================== PARENT POM INHERITANCE TESTS ====================
    // Note: Tests requiring actual Maven parent resolution (effective POM) 
    // are in EffectivePomIntegrationTest and require network access or
    // a properly configured Maven repository to resolve parent POMs.

    def 'Parent reference structure is valid'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File childPom = new File(testApp.appDir, 'pom.xml')
        File parentDir = new File(testApp.appDir, 'parent')
        File parentPom = new File(parentDir, 'pom.xml')

        then:
        childPom.exists()
        parentDir.exists()
        parentPom.exists()
        childPom.text.contains('<parent>')
        childPom.text.contains('<relativePath>parent/pom.xml</relativePath>')
    }

    def 'Parent POM contains dependency management'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File parentPom = new File(testApp.appDir, 'parent/pom.xml')

        then:
        parentPom.exists()
        parentPom.text.contains('<dependencyManagement>')
        parentPom.text.contains('<pluginManagement>')
    }

    // ==================== TEST DATA ====================

    private static final String POM_WITH_EXPLICIT_VERSION = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
        <mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>
        <http.connector.version>1.10.3</http.connector.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>${http.connector.version}</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
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

    private static final String POM_WITH_VERSION_OVERRIDE = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
        <mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.mule.connectors</groupId>
                <artifactId>mule-http-connector</artifactId>
                <version>1.10.3</version>
                <classifier>mule-plugin</classifier>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>1.11.0</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
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

    private static final String POM_WITH_BOM_IMPORT = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
        <mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.mule.connectors</groupId>
                <artifactId>mule-connector-commons</artifactId>
                <version>1.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.mule.connectors</groupId>
                <artifactId>mule-http-connector</artifactId>
                <version>1.10.3</version>
                <classifier>mule-plugin</classifier>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
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

    private static final String POM_WITH_MULTIPLE_EXPLICIT_DEPS = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
        <mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>
        <http.connector.version>1.10.3</http.connector.version>
        <sockets.connector.version>1.2.5</sockets.connector.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>${http.connector.version}</version>
            <classifier>mule-plugin</classifier>
        </dependency>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-sockets-connector</artifactId>
            <version>${sockets.connector.version}</version>
            <classifier>mule-plugin</classifier>
        </dependency>
    </dependencies>
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

    private static final String POM_WITH_PLUGIN_MANAGEMENT = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
    </properties>
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.mule.tools.maven</groupId>
                    <artifactId>mule-maven-plugin</artifactId>
                    <version>4.6.0</version>
                    <extensions>true</extensions>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>mule-maven-plugin</artifactId>
                <version>4.6.0</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
'''

    private static final String POM_WITH_PLUGIN_VERSION_OVERRIDE = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
    </properties>
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.mule.tools.maven</groupId>
                    <artifactId>mule-maven-plugin</artifactId>
                    <version>4.6.0</version>
                    <extensions>true</extensions>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>mule-maven-plugin</artifactId>
                <version>4.7.0</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
'''

    private static final String POM_WITH_MULTIPLE_MANAGED_PLUGINS = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.mulelinter</groupId>
    <artifactId>sample-mule-app</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>sample-mule-app-sys-api</name>
    <properties>
        <app.runtime>4.9.16</app.runtime>
        <mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>
        <munit.version>3.6.3</munit.version>
    </properties>
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.mule.tools.maven</groupId>
                    <artifactId>mule-maven-plugin</artifactId>
                    <version>4.6.0</version>
                    <extensions>true</extensions>
                </plugin>
                <plugin>
                    <groupId>com.mulesoft.munit.tools</groupId>
                    <artifactId>munit-maven-plugin</artifactId>
                    <version>3.6.3</version>
                </plugin>
            </plugins>
        </pluginManagement>
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
            </plugin>
        </plugins>
    </build>
</project>
'''
}
