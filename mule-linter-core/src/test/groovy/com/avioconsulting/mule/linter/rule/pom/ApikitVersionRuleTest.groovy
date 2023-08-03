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
        println(violations)
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
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
        println(violations)
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
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
\t\t\t<groupId>org.mule.modules</groupId>
\t\t\t<artifactId>mule-apikit-module</artifactId>
\t\t\t<version>1.9.1</version>
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
\t\t<apikit-version>1.8.2</apikit-version>
\t</properties>
\t<dependencies>
\t\t<dependency>
\t\t\t<groupId>org.mule.modules</groupId>
\t\t\t<artifactId>mule-apikit-module</artifactId>
\t\t\t<version>${apikit-version}</version>
\t\t\t<classifier>mule-plugin</classifier>
\t\t</dependency>
\t</dependencies>
</project>
'''
}