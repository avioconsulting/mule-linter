package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.model.Namespace

class FlowComponent extends MuleComponent {

    final static String COMPONENT_NAMESPACE = Namespace.CORE
    final static String COMPONENT_NAME_FLOW = 'flow'
    final static String COMPONENT_NAME_SUBFLOW = 'sub-flow'
    static final String APIKIT_FLOW_PREFIX_REGEX = '(get:|post:|put:|patch:|delete:|head:|options:|trace:).*'

    FlowComponent(String componentName, String componentNamespace, Map<String, String> attributes, File file,
                  List<MuleComponent> children) {
        super(componentName, componentNamespace, attributes, file, children)
    }

    Boolean hasSource() {
        return SOURCES.any({children[0].componentName.contains(it)})
    }

    Boolean isApiKitFlow() {
        return name.matches(APIKIT_FLOW_PREFIX_REGEX)
    }

    static final String[] SOURCES = [
            "listener", // HTTP, Sockets, File, FTP, SFTP, Email, JMS, VM,
            "scheduler", // Scheduler (Core)
            "subscriber", // Anypoint MQ
            "trigger", "new-object", "modified-object", "deleted-object", // Salesforce, Netsuite, Workday, LDAP, S3
            "receivemessages" // SQS
    ]
}
