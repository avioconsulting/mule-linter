package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult

/**
 * This class represents a Mule Configuration XML file.
 */
class ConfigurationFile extends ProjectFile {

    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists
    private List<LoggerComponent> loggerComponents = []
    private List<MuleComponent> components = []

    ConfigurationFile(File file) {
        super(file)
        println("ConfigurationFile: adding file $file.name")
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
            findLoggers()
//            components = parseChildren(configXml)
        } else {
            println('File does not exist....')
            exists = false
        }
    }

    // TODO new method name.... 'Find me all components that are not...'
    // notComponents(['sub-flow','flow'])
    List<MuleComponent> notComponents(List<String> ignoreComponents){
        List<MuleComponent> compList = []
        GPathResult[] comps = configXml.depthFirst().findAll {
            !ignoreComponents.contains(it.name())
            //TODO need the namespace...
        }

        comps.each { comp ->
            Map<String,String> atts = [:]
            comp[0].attributes.each {
                atts.put(it.key, it.value)
            }
            compList.add(new MuleComponent(atts))
        }
    }

    //TODO does not work...
    private List<MuleComponent> parseChildren(GPathResult path) {
        List<MuleComponent> comps = []
        Map<String,String> atts = [:]
        path[0].attributes.each {
            atts.put(it.key, it.value)
        }
        println atts
        if(path.children().size() > 0) {
            List<MuleComponent> childList = []
            path.children().each {
                childList = parseChildren(it)
            }
            comps.add(new MuleComponent(atts, childList))
        } else {
            comps.add(new MuleComponent(atts))
        }
    }

    private void findLoggers() {
        //TODO remove depthFirst call
        GPathResult[] loggers = configXml.depthFirst().findAll {
            it.name() == 'logger' && it.namespaceURI() == 'http://www.mulesoft.org/schema/mule/core'
        }
        loggers.each { log ->
            Map<String,String> atts = [:]
            log[0].attributes.each {
                atts.put(it.key, it.value)
            }
            loggerComponents.add(new LoggerComponent(atts))
        }
    }

    List<LoggerComponent> getLoggerComponents() {
        return loggerComponents
    }

    void setLoggerComponents(List<LoggerComponent> loggerComponents) {
        this.loggerComponents = loggerComponents
    }


}
