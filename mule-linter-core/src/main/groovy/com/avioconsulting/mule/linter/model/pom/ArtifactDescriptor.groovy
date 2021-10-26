package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult

class ArtifactDescriptor {

    String groupId
    String artifactId
    PomElement version
    Integer lineNo
    PomFile pomFile
    GPathResult pluginXml

    ArtifactDescriptor(GPathResult pluginXml, PomFile pomFile) {
        this.pluginXml = pluginXml
        this.groupId = pluginXml.groupId as String
        this.artifactId = pluginXml.artifactId as String
        this.lineNo = MuleXmlParser.getNodeLineNumber(pluginXml)
        this.pomFile = pomFile
        this.version = getAttribute('version')
    }

    PomElement getAttribute(String attributeName) {
        PomElement pElement = null
        GPathResult element = pluginXml.depthFirst().find {
            it.name() == attributeName
        }

        if (element != null ) {
            if ( isExpression(element.text()) ) {
                pElement = pomFile.getPomProperty(variableName(element.text()))
            } else {
                pElement = new PomElement()
                pElement.name = element.name()
                pElement.value = element.text()
                pElement.lineNo = MuleXmlParser.getNodeLineNumber(element)
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
