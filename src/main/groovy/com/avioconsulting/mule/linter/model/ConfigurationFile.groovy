package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.parser.MuleXmlParser
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.Node

/**
 * This class represents a Mule Configuration XML file.
 */
@SuppressWarnings('SpaceAroundMapEntryColon')
class ConfigurationFile extends ProjectFile {

    static final String ELEMENT_FLOWREF = 'flow-ref'
    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists
    private Map<String, String> nonGlobalConfig = ['sub-flow'     : Namespace.CORE,
                                                   'flow'         : Namespace.CORE,
                                                   'error-handler': Namespace.CORE]

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
            componentList.add(new MuleComponent(comp.name(), comp.namespaceURI(), comp.attributes(), getFile(),
                    getNestedComponent(comp)))
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
            componentList.add(new MuleComponent(comp.name(), comp.namespaceURI(), comp.attributes(), getFile(), getNestedComponent(comp)))
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
            componentList.add(new MuleComponent(comp[0].name(), comp[0].namespaceURI(), comp[0].attributes(), getFile(),
                    getNestedComponent(comp)))
        }
        return componentList
    }

    List<MuleComponent> getNestedComponent(GPathResult comp) {
        List<MuleComponent> componentList = []
        comp.children().each {
            componentList.add(new MuleComponent(it.name(), it.namespaceURI(), it.attributes(), getFile(),
                    getNestedComponent(it)))
        }
        return componentList
    }

    List<MuleComponent> getNestedComponent(Node comp) {
        List<MuleComponent> componentList = []
        comp.childNodes().each {
            componentList.add(new MuleComponent(it.name(), it.namespaceURI(), it.attributes(), getFile(),
                    getNestedComponent((Node) it)))
        }
        return componentList
    }

    List<LoggerComponent> findLoggerComponents() {
        List<LoggerComponent> loggerComponents = []
        searchComponentType(LoggerComponent.COMPONENT_NAME, LoggerComponent.COMPONENT_NAMESPACE).each {
            loggerComponents.add(new LoggerComponent(it[0].attributes, getFile()))
        }
        return loggerComponents
    }

    List<FlowComponent> getFlows() {
        List<FlowComponent> comps = []
        searchComponentType(FlowComponent.COMPONENT_NAME_FLOW, FlowComponent.COMPONENT_NAMESPACE).each {
            comp ->
                comps.add(new FlowComponent(comp[0].name(), comp[0].namespaceURI(), comp[0].attributes(), getFile(),
                        getNestedComponent(comp)))
        }
        return comps
    }

    List<FlowComponent> getSubFlows() {
        List<FlowComponent> comps = []
        searchComponentType(FlowComponent.COMPONENT_NAME_SUBFLOW, FlowComponent.COMPONENT_NAMESPACE).each {
            comp ->
                comps.add(new FlowComponent(comp[0].name(), comp[0].namespaceURI(), comp[0].attributes(), getFile(),
                        getNestedComponent(comp)))
        }
        return comps
    }

    List<MuleComponent> getFlowrefs() {
        return findComponents(ELEMENT_FLOWREF, Namespace.CORE)
    }

    List<FlowComponent> getAllFlows() {
        return flows + subFlows
    }

    private List<GPathResult> searchComponentType(String componentType, String namespace) {
        List<GPathResult> components = configXml.depthFirst().findAll {
            it.name() == componentType && it.namespaceURI() == namespace
        }
        return components
    }

}
