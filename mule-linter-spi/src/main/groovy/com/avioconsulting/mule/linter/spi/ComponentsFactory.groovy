package com.avioconsulting.mule.linter.spi

import com.avioconsulting.mule.linter.model.configuration.ComponentIdentifier
import com.avioconsulting.mule.linter.model.configuration.MuleComponent

interface ComponentsFactory {

    Set<ComponentIdentifier> registeredComponents()

    boolean hasComponentFor(ComponentIdentifier identifier)

    MuleComponent getComponentFor(ComponentIdentifier identifier, Map<String, String> attributes, File file)

    MuleComponent getComponentFor(ComponentIdentifier identifier, Map<String, String> attributes, File file,
                                  List<MuleComponent> children)
}