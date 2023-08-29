package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.Namespace

class LoggerComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = Namespace.CORE
    final static String COMPONENT_NAME = 'logger'
    private final String docName
    private final String message
    private final LogLevel level
    private final String category
    private static final ComponentIdentifier IDENTIFIER_LOGGER = new ComponentIdentifier(COMPONENT_NAME, COMPONENT_NAMESPACE)

    static boolean accepts(ComponentIdentifier identifier) {
        return IDENTIFIER_LOGGER.equals(identifier)
    }

    static enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    LoggerComponent(Map<String, String> attributes, File file) {
        super(COMPONENT_NAME, COMPONENT_NAMESPACE, attributes,file)
        this.docName = attributes.get('{http://www.mulesoft.org/schema/mule/documentation}name')
        this.message = attributes.get('message')
        this.level = LogLevel.valueOf attributes.get('level')
        this.category = attributes.get('category')
    }

    LoggerComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file) {
        super(componentName, componentNamespace, attributes,file)
        this.docName = attributes.get('{http://www.mulesoft.org/schema/mule/documentation}name')
        this.message = attributes.get('message')
        // In AVIO Custom logger, since log level is not populated as attribute for default log level - 'INFO'
        // Check for log level attribute, and if it doesn't exist map it to default 'INFO'.
        if (attributes.get('level') != null){
            this.level = LogLevel.valueOf attributes.get('level')
        }else{
            this.level = LogLevel.valueOf 'INFO'
        }

        this.category = attributes.get('category')
    }

    String getName() {
        return docName
    }

    String getMessage() {
        return message
    }

    String getLevel() {
        return level
    }

    String getCategory() {
        return category
    }

}
