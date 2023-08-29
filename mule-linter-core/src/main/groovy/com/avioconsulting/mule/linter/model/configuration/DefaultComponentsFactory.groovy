package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.spi.ComponentsFactory

class DefaultComponentsFactory implements ComponentsFactory{
    @Override
    Set<ComponentIdentifier> registeredComponents() {
        return null
    }

    @Override
    boolean hasComponentFor(ComponentIdentifier identifier) {
        return FlowComponent.accepts(identifier) || LoggerComponent.accepts(identifier) || AVIOLoggerComponent.accepts(identifier)
    }

    @Override
    MuleComponent getComponentFor(ComponentIdentifier identifier, Map<String, String> attributes, File file) {
       return getComponentFor(identifier, attributes, file, null)
    }

    @Override
    MuleComponent getComponentFor(ComponentIdentifier identifier, Map<String, String> attributes, File file, List<MuleComponent> children) {
        if (FlowComponent.accepts(identifier)){
            return new FlowComponent(identifier.name, identifier.namespaceURI, attributes, file, children)
        } else if(LoggerComponent.accepts(identifier)) {
            return new LoggerComponent(attributes, file)
        } else if(AVIOLoggerComponent.accepts(identifier)) {
            return new AVIOLoggerComponent(attributes, file)
        } else {
            return null
        }
    }
}
