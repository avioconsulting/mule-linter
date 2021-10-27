package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class MunitVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Correct MUnit Version'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Incorrect MUnit Version'() {
        given:
        testApp.addPom()
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    def 'Missing MUnit Property'() {
        given:
        testApp.addFile(PomFile.POM_XML, INVALID_POM)
        MuleApplication app = new MuleApplication(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 11
    }

    private static final String INVALID_POM = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.avioconsulting.mulelinter</groupId>
  <artifactId>sample-mule-app</artifactId>
  <version>1.0.0</version>
  <packaging>mule-application</packaging>
  <name>sample-mule-app-sys-api</name>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <app.runtime>4.2.1</app.runtime>
    <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
  </properties>
</project>'''
}
