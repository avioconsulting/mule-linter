package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.TestApplication
import spock.lang.Specification

class MunitVersionRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def cleanup() {
        testApp.remove()
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Correct MUnit Version'() {
        given:
        testApp.addPom()
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('2.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Incorrect MUnit Version'() {
        given:
        testApp.addPom()
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 16
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Missing MUnit Property'() {
        given:
        testApp.addFile(TestApplication.POM_FILE, invalidPom)
        Application app = new Application(testApp.appDir)

        when:
        Rule rule = new MunitVersionRule('3.2.1')
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].lineNumber == 11
    }

    def invalidPom = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
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
