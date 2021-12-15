package com.avioconsulting.mule.linter.extension.components

import com.avioconsulting.mule.linter.model.configuration.ComponentIdentifier
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.spi.ComponentsFactory


class AVIOComponentsFactory implements ComponentsFactory {

    @Override
    Set<ComponentIdentifier> registeredComponents() {
        return null
    }

    @Override
    boolean hasComponentFor(ComponentIdentifier componentIdentifier) {
        return HttpComponent.accepts(componentIdentifier)
    }

    @Override
    MuleComponent getComponentFor(ComponentIdentifier componentIdentifier, Map<String, String> attributes, File file) {
        return getComponentFor(componentIdentifier, attributes, file, null)
    }

    @Override
    MuleComponent getComponentFor(ComponentIdentifier componentIdentifier, Map<String, String> attributes, File file, List<MuleComponent> children) {
        return new HttpComponent(componentIdentifier.name, componentIdentifier.namespaceURI, attributes, file, children)
    }
}
