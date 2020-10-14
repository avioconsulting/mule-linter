package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Version
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class PomDependencyVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing Maven Dependency'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_DEPENDENCY_POM)
        Rule rule = new PomDependencyVersionRule('org.mule.connectors', 'mule-http-connector', '1.3.2')

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
        violations[0].message.startsWith(PomDependencyVersionRule.MISSING_DEPENDENCY)
    }

    def 'Correct Equal Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)
        Rule rule = new PomDependencyVersionRule('org.mule.connectors', 'mule-http-connector', '1.3.2')

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Not Greater Than Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)

        Rule rule = new PomDependencyVersionRule('org.mule.connectors', 'mule-http-connector', '1.3.9',
                Version.Operator.GREATER_THAN)

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
        violations[0].message.startsWith(PomDependencyVersionRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Multiple Greater Than Maven Dependency version'() {
        given:
        testApp.addFile(PomFile.POM_XML, WITH_DEPENDENCY_POM)

        Rule rule = new PomDependencyVersionRule('org.mule.connectors', 'mule-http-connector', version,
                Version.Operator.GREATER_THAN)

        when:
        app = new Application(testApp.appDir)
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
        Rule rule = new PomDependencyVersionRule('org.mule.connectors', 'mule-http-connector', '1.3.2',
                Version.Operator.EQUAL)

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    private static final String MISSING_DEPENDENCY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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

    private static final String WITH_DEPENDENCY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
\t<dependencies>
\t\t<dependency>
\t\t\t<groupId>org.mule.connectors</groupId>
\t\t\t<artifactId>mule-http-connector</artifactId>
\t\t\t<version>1.3.2</version>
\t\t\t<classifier>mule-plugin</classifier>
\t\t</dependency>
\t</dependencies>
</project>
'''
    private static final String DEPENDENCY_PROPERTY_VERSION_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
\t\t<http-connector>1.3.2</http-connector>
\t</properties>
\t<dependencies>
\t\t<dependency>
\t\t\t<groupId>org.mule.connectors</groupId>
\t\t\t<artifactId>mule-http-connector</artifactId>
\t\t\t<version>${http-connector}</version>
\t\t\t<classifier>mule-plugin</classifier>
\t\t</dependency>
\t</dependencies>
</project>
'''
}