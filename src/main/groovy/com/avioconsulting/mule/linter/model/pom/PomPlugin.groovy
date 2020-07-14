package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult

class PomPlugin {

    String groupId
    String artifactId
    String version
    Integer lineNo
    PomFile pomFile
    GPathResult pluginXml

    PomPlugin(GPathResult pluginXml, PomFile pomFile) {
        this.pluginXml = pluginXml
        this.groupId = pluginXml.groupId as String
        this.artifactId = pluginXml.artifactId as String
        this.lineNo = MuleXmlParser.getNodeLineNumber(pluginXml)
        this.pomFile = pomFile
        this.version = isExpression(pluginXml.version as String) ?
                resolveExpression(pluginXml.version as String) : pluginXml.version
    }

    PomElement getConfigProperty(String propertyName) {
        PomElement pElement = null
        pluginXml.configuration.depthFirst().each {
            if (it.name() == propertyName) {
                pElement = new PomElement()
                pElement.name = propertyName
                pElement.value = it.text()
                pElement.lineNo = MuleXmlParser.getNodeLineNumber(it)
            }
        }
        return pElement
    }

    private Boolean isExpression(String expression) {
        expression.startsWith('${')
    }

    private String variableName(String expression) {
        expression.takeAfter('${').takeBefore('}')
    }

    private String resolveExpression(String expression) {
        try {
            return pomFile.getPomProperty(variableName(expression)).value
        } catch (IllegalArgumentException iae) {
            return null
        }
    }

}
