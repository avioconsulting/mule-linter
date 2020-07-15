package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.MuleComponent

class FlowComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = 'http://www.mulesoft.org/schema/mule/core'
    final static String COMPONENT_NAME_FLOW = 'flow'
    final static String COMPONENT_NAME_SUBFLOW = 'sub-flow'
    static final String APIKIT_FLOW_PREFIX_REGEX = '(get:|post:|put:|patch:|delete:|head:|options:|trace:).*'

    FlowComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file,
                  List<MuleComponent> children) {
        super(componentName, componentNamespace, attributes, file, children)
    }

    Boolean hasListner() {
        return children[0].componentName.contains('listener')
    }

    Boolean isApiKitFlow() {
        return name.matches(APIKIT_FLOW_PREFIX_REGEX)
    }

}
