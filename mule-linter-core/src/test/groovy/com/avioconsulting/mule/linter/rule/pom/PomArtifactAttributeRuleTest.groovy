package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class PomArtifactAttributeRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()
    private Application app

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Missing Maven Plugin'() {
        given:
        testApp.addFile(PomFile.POM_XML, MISSING_PLUGINS_POM)
        Rule rule = new PomPluginAttributeRule('org.mule.tools.maven', 'mule-maven-plugin', ['version':'3.3.5'])

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
        violations[0].message.startsWith(PomPluginAttributeRule.MISSING_PLUGIN)
    }

    def 'Correct Maven Plugin version'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_POM)
        Rule rule = new PomPluginAttributeRule('org.mule.tools.maven', 'mule-maven-plugin', ['version':'3.3.5'])

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Maven Plugin version'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_POM)
        Rule rule = new PomPluginAttributeRule('org.mule.tools.maven', 'mule-maven-plugin', ['version':'3.3.6'])

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
        violations[0].lineNumber == 17
        violations[0].message.startsWith(PomPluginAttributeRule.RULE_VIOLATION_MESSAGE)
    }

    def 'Correct Maven Plugin version check as property'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_WITHPROPERTY_POM)
        Rule rule = new PomPluginAttributeRule('org.mule.tools.maven', 'mule-maven-plugin', ['version':'3.3.5'])

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect Maven Plugin version check as property'() {
        given:
        testApp.addFile(PomFile.POM_XML, PLUGINS_EXISTS_WITHPROPERTY_POM)
        Rule rule = new PomPluginAttributeRule('org.mule.tools.maven', 'mule-maven-plugin', ['version':'3.3.6'])

        when:
        app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.contains('pom.xml')
        violations[0].lineNumber == 13
        violations[0].message.startsWith(PomPluginAttributeRule.RULE_VIOLATION_MESSAGE)
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

    private static final String PLUGINS_EXISTS_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
\t<build>
\t\t<plugins>
\t\t\t<plugin>
\t\t\t\t<groupId>org.mule.tools.maven</groupId>
\t\t\t\t<artifactId>mule-maven-plugin</artifactId>
\t\t\t\t<version>3.3.5</version>
\t\t\t\t<extensions>true</extensions>
\t\t\t\t<configuration>
\t\t\t\t</configuration>
\t\t\t</plugin>
\t\t</plugins>
\t</build>
</project>
'''

    private static final String PLUGINS_EXISTS_WITHPROPERTY_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
\t\t<mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
\t</properties>
\t<build>
\t\t<plugins>
\t\t\t<plugin>
\t\t\t\t<groupId>org.mule.tools.maven</groupId>
\t\t\t\t<artifactId>mule-maven-plugin</artifactId>
\t\t\t\t<version>${mule.maven.plugin.version}</version>
\t\t\t\t<extensions>true</extensions>
\t\t\t\t<configuration>
\t\t\t\t</configuration>
\t\t\t</plugin>
\t\t</plugins>
\t</build>
</project>
'''

}

