package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.model.Namespace
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
        this.lineNo = getNodeLineNumber(pluginXml)
        this.pomFile = pomFile
        this.version = getAttribute('version')
    }

    PomElement getAttribute(String attributeName) {
        GPathResult element = pluginXml.depthFirst().find {
            it.name() == attributeName
        }
        return getPomElement(element)
    }

    PomElement getConfigProperty(String propertyName) {
        GPathResult element = pluginXml.configuration.depthFirst().find {
            it.name() == propertyName
        }
        return getPomElement(element)
    }
    private PomElement getPomElement(GPathResult element) {
        PomElement pElement = null
        if (element != null ) {
            if ( isExpression(element.text()) ) {
                pElement = pomFile.getPomProperty(variableName(element.text()))
            } else {
                pElement = new PomElement()
                pElement.name = element.name()
                pElement.value = element.text()
                pElement.lineNo = getNodeLineNumber(element)
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

    static Integer getNodeLineNumber(GPathResult node) {
        return Integer.valueOf(String.valueOf(node["@${Namespace.START_LINE_NO_NAMESPACE_PREFIX}:${Namespace.START_LINE_NO_ATTRIBUTE}"]))
    }
}
