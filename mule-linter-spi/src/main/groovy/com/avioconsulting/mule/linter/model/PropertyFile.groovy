package com.avioconsulting.mule.linter.model

import org.yaml.snakeyaml.Yaml

class PropertyFile {

    File file
    String name
//    Properties properties
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
            parseYamlSimpleList(newKey, (List<?>) o)
        } else {
            properties.put(newKey, o.toString());
        }
    }

    //  For when complex list objects are implemented
    private void parseYaml(String val, List list) {
        for (def i = 0; i < list.size(); i++) {
            yamlParseHelper(String.format("%s[%s]", val, i), list.get(i))
        }
    }

    //  MuleSoft as of 4.x does not allow complex list objects as properties and puts them as a CSV list
    private void parseYamlSimpleList(String val, List list) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i)
            if (item instanceof Map || item instanceof List) {
                //  This will throw an error in mule, but in an effort to avoid breaking the build
                //  we will parse it in case that changes in the future
                parseYaml(val, list)
            } else {
                sb.append(item)
                if (i != list.size() - 1) {
                    sb.append(",")
                }
            }
        }
        yamlParseHelper(val, sb.toString())
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

//    void loadProperties() {
//        Properties props = new Properties()
//        file.withInputStream {
//            props.load(it)
//        }
//        properties = props
//    }

    File getFile() {
        return file
    }

    Hashtable<Object, Object> getProperties() {
        return properties
    }

//    Properties getProperties() {
//        return properties
//    }

    Integer getPropertyCount() {
        return properties.size()
    }

    String getProperty(String propertyName) {
//        return properties.getProperty(propertyName)
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
