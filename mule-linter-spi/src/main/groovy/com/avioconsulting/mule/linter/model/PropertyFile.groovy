package com.avioconsulting.mule.linter.model

import org.yaml.snakeyaml.Yaml

class PropertyFile {

    File file
    String name
    Hashtable<Object, Object> properties

    PropertyFile(File file) {
        this.file = file
        this.name = file.name
        loadProperties()
    }

    private void readProps() {
        Properties props = new Properties()
        file.withInputStream {
            props.load(it)
        }
        properties = props
    }

    private void yamlParseHelper(String newKey, def o) {
        if (o instanceof Map) {
            parseYaml(newKey, (Map<?, ?>) o)
        } else if (o instanceof List) {
            parseYaml(newKey, (List<?>) o)
        } else {
            properties.put(newKey, o.toString());
        }
    }

    private void parseYaml(String val, List list) {
        for (def i = 0; i < list.size(); i++) {
            yamlParseHelper(String.format("%s.%s", val, i), list.get(i))
        }
    }

    private void parseYaml(String val, Map<?, ?> map) {
        map.forEach((key, value) ->
                yamlParseHelper((val.isEmpty() ? key.toString() : String.format("%s.%s", val, key.toString())), value)
        )
    }

    void loadProperties() {
        if (name.endsWith(".properties")) {
           readProps()
        } else if (name.endsWith(".yaml")) {
            properties = new Hashtable<>()
            file.withInputStream {
                parseYaml("", (Map<String, ?>) new Yaml().load(it))
            }
        }
        else {
            throw new IllegalStateException("Invalid property file ${name}")
        }
    }

    File getFile() {
        return file
    }

    Hashtable<Object, Object> getProperties() {
        return properties
    }

    Integer getPropertyCount() {
        return properties.size()
    }

    String getProperty(String propertyName) {
        return properties.get(propertyName)
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
