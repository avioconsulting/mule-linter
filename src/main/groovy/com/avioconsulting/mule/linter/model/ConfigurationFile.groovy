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

    ConfigurationFile(File file) {
        super(file)
        println("ConfigurationFile: adding file $file.name")
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
            findLoggers()
        } else {
            println('File does not exist....')
            exists = false
        }
    }

    private void findLoggers() {
        GPathResult[] loggers = configXml.depthFirst().findAll {
            it.name() == 'logger'


        }
        loggers.each { log ->
//            println('New Logger: ' + log.@'doc:name' + '|' + log.@message + '|' + log.@level + '|' + log.@category + '|' + parser.getNodeLineNumber(log))
//            println('Logger namespace: ' + log.namespacePrefix)
//            loggerComponents.add(new LoggerComponent(it.@'doc:name'.toString(), it.@message.toString(), it.@level.toString(), it.@category.toString(), parser.getNodeLineNumber(it)))
            Map<String,String> atts = [:]
            log[0].attributes.each {
                atts.put(it.key, it.value)
//                println 'Adding ' + it.key + ' with value ' + it.value
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
