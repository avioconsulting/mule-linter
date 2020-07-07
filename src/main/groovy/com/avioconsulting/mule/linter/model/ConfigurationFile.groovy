package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult

/**
 * This class represents a Mule Configuration XML file.
 */
class ConfigurationFile extends ProjectFile {

    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists

    ConfigurationFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
        } else {
            exists = false
        }
    }

    List<MuleComponent> findComponents(String componentType, String namespace) {
        List<MuleComponent> componentList = []
        GPathResult[] loggers = configXml.depthFirst().findAll {
            it.name() == componentType && it.namespaceURI() == namespace
        }
        loggers.each { log ->
            Map<String, String> atts = [:]
            log[0].attributes.each {
                atts.put(it.key, it.value)
            }
            componentList.add(new MuleComponent(atts))
            //TODO this doesn't account for child components...
        }
    }

    List<LoggerComponent> findLoggerComponents() {
        List<LoggerComponent> loggerComponents = []
        GPathResult[] loggers = configXml.depthFirst().findAll {
            it.name() == 'logger' && it.namespaceURI() == 'http://www.mulesoft.org/schema/mule/core'
        }

        loggers.each { log ->
            Map<String, String> atts = [:]
            log[0].attributes.each {
                atts.put(it.key, it.value)
            }
            loggerComponents.add(new LoggerComponent(atts))
        }
        return loggerComponents
    }

}
