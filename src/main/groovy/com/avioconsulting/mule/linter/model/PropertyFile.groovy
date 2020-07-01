package com.avioconsulting.mule.linter.model

class PropertyFile {

    File file
    String name
    Properties properties

    PropertyFile(File file) {
        this.file = file
        this.name = file.name
        loadProperties()
    }

    void loadProperties() {
        // TODO Odd scenario, where if i use the properties variable here, I get an error:
        //  Cannot invoke method load() on null object
        Properties props = new Properties()
        file.withInputStream {
            props.load(it)
        }
        properties = props
    }

    File getFile() {
        return file
    }

    Integer getPropertyCount() {
        return properties.size()
    }

    String getProperty(String propertyName) {
        return properties.getProperty(propertyName)
    }

    void setFile(File file) {
        this.file = file
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

}
