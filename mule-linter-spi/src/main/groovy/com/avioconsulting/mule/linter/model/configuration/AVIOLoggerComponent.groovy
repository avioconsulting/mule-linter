package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.Namespace

class AVIOLoggerComponent extends LoggerComponent {

    final static String COMPONENT_NAMESPACE = Namespace.AVIO_LOGGER
    final static String COMPONENT_NAME = 'log'
    private static final ComponentIdentifier IDENTIFIER_LOGGER = new ComponentIdentifier(COMPONENT_NAME, COMPONENT_NAMESPACE)

    private final String correlationId
    private final String messageAttributes

    static boolean accepts(ComponentIdentifier identifier) {
        return IDENTIFIER_LOGGER.equals(identifier)
    }

    AVIOLoggerComponent(Map<String, String> attributes, File file) {
        super(COMPONENT_NAME, COMPONENT_NAMESPACE, attributes,file)
        this.correlationId = attributes.get('correlationId')
        this.messageAttributes = attributes.get('messageAttributes')
    }

    String getCorrelationId() {
        return correlationId
    }

    String getMessageAttributes() {
        return messageAttributes
    }
}