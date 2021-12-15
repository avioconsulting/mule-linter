package com.avioconsulting.mule.linter.model.configuration

import com.avioconsulting.mule.linter.spi.ComponentsFactory

class ComponentFactoryService {
    private static ComponentFactoryService service;
    private ServiceLoader<ComponentsFactory> loader;
    private final List<ComponentsFactory> factories;

    private ComponentFactoryService(){
        loader = ServiceLoader.load(ComponentsFactory.class);
        factories = loader.asList();
    }
    static synchronized ComponentFactoryService getInstance(){
        if(service == null) {
            service = new ComponentFactoryService();
        }
        return service;
    }

    MuleComponent getComponentFor(ComponentIdentifier identifier, Map<String, String> attributes, File file,
                                      List<MuleComponent> children) {
        def factory = factories.stream().find {it.hasComponentFor(identifier)}
        if(factory == null) {
            return new MuleComponent(identifier.name, identifier.namespaceURI, attributes, file, children)
        } else {
            return factory.getComponentFor(identifier, attributes, file, children)
        }
    }
}
