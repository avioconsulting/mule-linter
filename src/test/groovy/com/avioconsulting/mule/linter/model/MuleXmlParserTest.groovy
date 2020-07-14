package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult
import spock.lang.Specification

class MuleXmlParserTest extends Specification {

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def "Single line xml element"() {
        given:
        String xml = '''<test>
            <mule />
            </test>'''
        when:
        MuleXmlParser parser = new MuleXmlParser()
        GPathResult test = parser.parseText(xml)

        then:
        parser.getNodeLineNumber(test) == 1
        parser.getNodeLineNumber(test.mule) == 2
    }

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def "Pom.xml Property"() {
        given:
        String pomXml = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.avioconsulting.bc</groupId>
    <artifactId>np-store-product-sys-api</artifactId>
    <version>1.0.0</version>
    <packaging>mule-application</packaging>
    <name>np-store-product-sys-api</name>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <app.runtime>4.2.1</app.runtime>
        <mule.maven.plugin.version>3.3.5</mule.maven.plugin.version>
        <munit.version>2.2.1</munit.version>
    </properties>
</project>
'''
        when:
        MuleXmlParser parser = new MuleXmlParser()
        GPathResult project = parser.parseText(pomXml)
        GPathResult appRuntime = project.'properties'.'app.runtime' as GPathResult
        GPathResult munitVersion = project.'properties'.'munit.version' as GPathResult
        String artifactId = project.getProperty('artifactId')

        then:
        artifactId == 'np-store-product-sys-api'
        appRuntime.text() == '4.2.1'
        parser.getNodeLineNumber(appRuntime) == 12

        munitVersion.text() == '2.2.1'
        parser.getNodeLineNumber(munitVersion) == 14
    }

}
