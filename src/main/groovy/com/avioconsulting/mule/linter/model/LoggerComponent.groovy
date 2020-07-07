package com.avioconsulting.mule.linter.model

class LoggerComponent extends MuleComponent {

    private String message
    private String level
    private String category

    LoggerComponent(Map<String, String> attributes) {
        super(attributes)
        this.name = attributes.get('{http://www.mulesoft.org/schema/mule/documentation}name')
        this.message = attributes.get('message')
        this.level = attributes.get('level')
        this.category = attributes.get('category')
    }

    @Override
    String getName() {
        return name
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
