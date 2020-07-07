package com.avioconsulting.mule.linter.model

class MuleComponent {

    private String name
    private Map<String, String> attributes = [:]
    private final List<MuleComponent> children

    MuleComponent(Map<String, String> attributes) {
        this(attributes, null)
    }

    MuleComponent(Map<String, String> attributes, List<MuleComponent> children) {
        this.attributes = attributes
        this.children = children
    }

    Boolean hasAttributeValue(String name) {
        return attributes.get(name)?.length() > 0
    }

    String getName() {
        if (name == null || name.length() < 1) {
            name = attributes.get('name')
        }
        return name
    }

    void setName(String name) {
        this.name = name
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
