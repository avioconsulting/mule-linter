package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult

class PomPlugin {

    String groupId
    String artifactId
    PomElement version
    Integer lineNo
    PomFile pomFile
    GPathResult pluginXml

    PomPlugin(GPathResult pluginXml, PomFile pomFile) {
        this.pluginXml = pluginXml
        this.groupId = pluginXml.groupId as String
        this.artifactId = pluginXml.artifactId as String
        this.lineNo = MuleXmlParser.getNodeLineNumber(pluginXml)
        this.pomFile = pomFile
        this.version = getVersion()
    }

    PomElement getVersion() {
        PomElement pElement = null
        GPathResult version = pluginXml.depthFirst().find {
            it.name() == 'version'
        }

        if (version != null ) {
            if ( isExpression(version.text()) ) {
                pElement = pomFile.getPomProperty(variableName(version.text()))
            } else {
                pElement = new PomElement()
                pElement.name = version.name()
                pElement.value = version.text()
                pElement.lineNo = MuleXmlParser.getNodeLineNumber(version)
            }
        }

        return pElement
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

}
