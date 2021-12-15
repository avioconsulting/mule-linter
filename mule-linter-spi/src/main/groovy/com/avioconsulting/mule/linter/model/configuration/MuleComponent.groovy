package com.avioconsulting.mule.linter.model.configuration

class MuleComponent {
    public static final String START_LINE_NO_ATTRIBUTE = '_startLineNo'
    public static final String START_LINE_NO_NAMESPACE = 'http://www.avioconsulting.com/mule/linter'
    private final ComponentIdentifier componentIdentifier
    final Map<String, String> attributes = [:]
    private final List<MuleComponent> children
    private final File file

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file) {
        this(componentName, componentNamespace, attributes, file, null)
    }

    MuleComponent(ComponentIdentifier identifier, Map<String, String> attributes, File file, List<MuleComponent> children) {
        this.componentIdentifier = identifier
        this.attributes = attributes
        this.children = children
        this.file = file
    }

    MuleComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file,
                  List<MuleComponent> children) {
        this(new ComponentIdentifier(componentName, componentNamespace), attributes, file, children);
    }

    Boolean hasAttributeValue(String name) {
        return attributes.get(name)?.length() > 0
    }

    String getAttributeValue(String name) {
        return attributes.get(name)
    }

    String getComponentName() {
        return componentIdentifier.name
    }

    Integer getLineNumber() {
        String lineNoString = '{' + START_LINE_NO_NAMESPACE + '}' + START_LINE_NO_ATTRIBUTE
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

    static boolean accepts(ComponentIdentifier identifier) {
        return  true
    }
}
