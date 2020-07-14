package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.parser.MuleXmlParser

class MuleComponent {

    private final String componentName
    private final String componentNamespace
    final Map<String, String> attributes = [:]
    private final List<MuleComponent> children

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes) {
        this(componentName, componentNamespace, attributes, null)
    }

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes,
                  List<MuleComponent> children) {
        this.componentName = componentName
        this.componentNamespace = componentNamespace
        this.attributes = attributes
        this.children = children
    }

    Boolean hasAttributeValue(String name) {
        return attributes.get(name)?.length() > 0
    }

    String getAttributeValue(String name) {
        return attributes.get(name)
    }

    String getComponentName() {
        return componentName
    }

    Integer getLineNumber() {
        String lineNoString = '{' + MuleXmlParser.START_LINE_NO_NAMESPACE + '}' + MuleXmlParser.START_LINE_NO_ATTRIBUTE
        return Integer.parseInt(attributes.get(lineNoString))
    }

    List<MuleComponent> getChildren() {
        return children
    }

    String getId() {
        return attributes.get('id')
    }

}
