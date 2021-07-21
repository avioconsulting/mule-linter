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
        Properties props = new Properties()
        file.withInputStream {
            props.load(it)
        }
        properties = props
    }

    File getFile() {
        return file
    }

    Properties getProperties() {
        return properties
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


    @Override
    String toString() {
        return "PropertyFile{" +
                "file=" + file +
                '}';
    }
}
