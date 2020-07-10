package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.Node

/**
 * This class represents a Mule Configuration XML file.
 */
class ConfigurationFile extends ProjectFile {

    static final String MULE_CORE_NAMESPACE = 'http://www.mulesoft.org/schema/mule/core'
    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists
    private Map<String, String> nonGlobalConfig = ['sub-flow':MULE_CORE_NAMESPACE,
                                                   'flow'    :MULE_CORE_NAMESPACE]

    ConfigurationFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
        } else {
            exists = false
            configXml = null
        }
    }

    Boolean doesExists() {
        return exists
    }

    void addAdditionalGlobalConfig(Map<String, String> nonGlobalConfig) {
        this.nonGlobalConfig += nonGlobalConfig
    }

    List<MuleComponent> findGlobalConfigs() {
        List<MuleComponent> componentList = []
        List<Node> childNodes = configXml.childNodes() as List<Node>
        List<Node> comps = []
        childNodes.each { node ->
            if (!checkElementExists(node, nonGlobalConfig)) {
                comps.add(node)
            }
        }

        comps.each { comp ->
            componentList.add(new MuleComponent(comp.name(), comp.namespaceURI(), comp.attributes()))
        }
        return componentList
    }

    List<MuleComponent> findNonGlobalConfigs() {
        List<MuleComponent> componentList = []
        List<Node> childNodes = configXml.childNodes() as List<Node>
        List<Node> comps = []
        childNodes.each { node ->
            if (checkElementExists(node, nonGlobalConfig)) {
                comps.add(node)
            }
        }

        comps.each { comp ->
            componentList.add(new MuleComponent(comp.name(), comp.namespaceURI(), comp.attributes()))
        }
        return componentList
    }

    /**
     * It iterate through elements and check if one of the element belongs to node.
     * @param node ChildNode.
     * @param elements map of element name and namespace.
     * @return the boolean value of element found or not.
     */
    Boolean checkElementExists(Node node, Map<String, String> elements) {
        Map<String, String> found = elements.findAll {
            it.key == node.name() && it.value == node.namespaceURI()
        }
        return (found.size() > 0)
    }

    List<MuleComponent> findComponents(String componentType, String namespace) {
        List<MuleComponent> componentList = []
        searchComponentType(componentType, namespace).each { comp ->
            componentList.add(new MuleComponent(comp[0].name(), comp[0].namespaceURI(), comp[0].attributes()))
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
