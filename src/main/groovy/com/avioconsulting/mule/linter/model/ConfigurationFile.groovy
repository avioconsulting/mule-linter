package com.avioconsulting.mule.linter.model


import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.Node

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

    Boolean doesExists() {
        return exists
    }

    List<MuleComponent> findChildComponents(Map<String, String> elements, Boolean include) {
        List<MuleComponent> componentList = []
        def childNodes = configXml.childNodes()
        def comps = []
        childNodes.each {
            node ->
            if (include) {
                if (checkElementExists( node, elements )) {
                    comps.add(node)
                }
            } else {
                if (!checkElementExists( node, elements )) {
                    comps.add(node)
                }
            }
        }

        comps.each { comp ->
            Map<String, String> atts = [:]
            comp.attributes.each {
                atts.put(it.key, it.value)
            }
            componentList.add(new MuleComponent(comp.name(), comp.namespaceURI(), atts))
        }
        return componentList
    }

    List<MuleComponent> findGlobalConfigs(Map<String, String> noneGlobalElements) {
        return findChildComponents(noneGlobalElements, false)
    }

    Boolean checkElementExists(Node node, Map<String, String> element) {
        Boolean exists
        Map<String, String> found = element.findAll {
            it.key == node.name && it.value == node.namespaceURI()
        }
        if (found.size() > 0) {
            exists = true
        } else {
            exists = false
        }
        return exists
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
        comps.each { comp ->
            Map<String, String> atts = [:]
            comp[0].attributes.each {
                atts.put(it.key, it.value)
            }
            componentList.add(new MuleComponent(comp.name(), atts))
            //TODO this doesn't account for child components...
        }
    }

    List<MuleComponent> containsConfiguration(String componentType, String namespace) {
        List<MuleComponent> componentList = []
        GPathResult[] comps = configXml.depthFirst().findAll {
            it.name() == componentType && it.namespaceURI() == namespace
        }
        return components
    }

}
