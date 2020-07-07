package com.avioconsulting.mule.linter.model


import groovy.xml.slurpersupport.GPathResult

/**
 * This class represents a Mule Configuration XML file.
 */
class ConfigurationFile extends ProjectFile {

    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists

    ConfigurationFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
        } else {
            exists = false
        }
    }

    List<MuleComponent> findComponents(String componentType, String namespace) {
        List<MuleComponent> componentList = []
        searchComponentType(componentType, namespace).each { comp ->
            componentList.add(new MuleComponent(comp[0].attributes))
            //TODO this doesn't account for child components...
        }
        return componentList
    }

    List<LoggerComponent> findLoggerComponents() {
        List<LoggerComponent> loggerComponents = []
        searchComponentType(LoggerComponent.COMPONENT_NAME, LoggerComponent.COMPONENT_NAMESPACE).each {
            loggerComponents.add(new LoggerComponent(it[0].attributes))
        }
        return loggerComponents
    }

    private List<GPathResult> searchComponentType(String componentType, String namespace) {
        List<GPathResult> components = configXml.depthFirst().findAll {
            it.name() == componentType && it.namespaceURI() == namespace
        }
        return components
    }

}
