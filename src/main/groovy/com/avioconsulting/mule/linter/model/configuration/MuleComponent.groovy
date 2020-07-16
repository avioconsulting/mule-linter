package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.parser.MuleXmlParser

class MuleComponent {

    private final String componentName
    private final String componentNamespace
    final Map<String, String> attributes = [:]
    private final List<MuleComponent> children
    private final File file

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file) {
        this(componentName, componentNamespace, attributes, file, null)
    }

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file,
                  List<MuleComponent> children) {
        this.componentName = componentName
        this.componentNamespace = componentNamespace
        this.attributes = attributes
        this.children = children
        this.file = file
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

    File getFile(){
        return file
    }

    Object getProperty(String propertyName) {
        MetaProperty meta = this.metaClass.getMetaProperty(propertyName)
        if (meta) {
            meta.getProperty(this)
        } else {
            return attributes.get(propertyName)
        }
    }

}
