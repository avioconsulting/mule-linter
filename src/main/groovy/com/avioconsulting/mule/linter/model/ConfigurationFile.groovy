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
        loggers.each {
            println('New Logger: ' + it.@'doc:name' + '|' + it.@message + '|' + it.@level + '|' + it.@category + '|' + parser.getNodeLineNumber(it))
            loggerComponents.add(new LoggerComponent(it.@'doc:name'.toString(), it.@message.toString(), it.@level.toString(), it.@category.toString(), parser.getNodeLineNumber(it)))
        }
    }

    List<LoggerComponent> getLoggerComponents() {
        return loggerComponents
    }

    void setLoggerComponents(List<LoggerComponent> loggerComponents) {
        this.loggerComponents = loggerComponents
    }
}
