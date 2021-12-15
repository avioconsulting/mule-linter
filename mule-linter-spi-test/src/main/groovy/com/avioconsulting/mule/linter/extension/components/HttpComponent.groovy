package com.avioconsulting.mule.linter.extension.components

import com.avioconsulting.mule.linter.model.configuration.ComponentIdentifier
import com.avioconsulting.mule.linter.model.configuration.MuleComponent

class HttpComponent extends MuleComponent {
    static final String HTTP = "http://www.mulesoft.org/schema/mule/http"
    final static ComponentIdentifier IDENTIFIER_LISTENER = new ComponentIdentifier("listener", HTTP)

    HttpComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file) {
        super(componentName, componentNamespace, attributes, file)
    }

    HttpComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file, List<MuleComponent> children) {
        super(componentName, componentNamespace, attributes, file, children)
    }

    static boolean accepts(ComponentIdentifier identifier) {
        return IDENTIFIER_LISTENER.equals(identifier)
    }

    String getPath() {
        return getAttributeValue("path")
    }

    String getMethod() {
        return getAttributeValue("method")
    }
}
