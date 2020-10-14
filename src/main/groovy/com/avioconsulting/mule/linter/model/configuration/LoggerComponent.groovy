package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.Namespace

class LoggerComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = Namespace.CORE
    final static String COMPONENT_NAME = 'logger'
    private final String docName
    private final String message
    private final LogLevel level
    private final String category

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
