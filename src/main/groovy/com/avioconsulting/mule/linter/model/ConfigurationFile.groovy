package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult

/**
 * This class represents a Mule Configuration XML file.
 */
class ConfigurationFile extends ProjectFile {

    MuleXmlParser parser
    private final GPathResult configXml
    private final Boolean exists
    private final List<String> rootElement = []

    ConfigurationFile(File file) {
        super(file)
        println("ConfigurationFile: adding file $file.name")
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            configXml = parser.parse(file)
            loadElement()
        } else {
            println('File does not exist....')
            exists = false
        }
    }

    Boolean doesExist() {
        return exists
    }

    void loadElement() {
        rootElement.clear()
        if (exists) {
            configXml.children().each {
                element->
                    rootElement.add(element.name())
            }
        }
    }

    Boolean containsConfiguration(String configuration) {
        return rootElement.contains(configuration)
    }

    String findSomething(String element){
        println("looking for $element")
        return configXml.getProperty(element).toString()
    }

    String findAnother() {
        GPathResult subflow = configXml.'sub-flow'
//        subflow.each {
            println("subflow - names: $subflow.@name")
//        }
//        println("looking for subflow name: $subflow.@name")
    }


}
