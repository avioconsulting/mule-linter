package com.avioconsulting.mule.linter.model

class LoggerComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = 'http://www.mulesoft.org/schema/mule/core'
    final static String COMPONENT_NAME = 'logger'
    private final String docName
    private final String message
    private final String level
    private final String category

    LoggerComponent(Map<String, String> attributes) {
        super(attributes)
        this.docName = attributes.get('{http://www.mulesoft.org/schema/mule/documentation}name')
        this.message = attributes.get('message')
        this.level = attributes.get('level')
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
