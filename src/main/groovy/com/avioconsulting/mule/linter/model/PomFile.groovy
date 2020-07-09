package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult

class PomFile extends ProjectFile {

    public static final String PROPERTIES = 'properties'
    MuleXmlParser parser
    private final GPathResult pomXml
    private final Boolean exists

    PomFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
            parser = new MuleXmlParser()
            pomXml = parser.parse(file)
        } else {
            exists = false
            pomXml = null
        }
    }

    PomFile(File application, String fileName) {
        this(new File(application, fileName))
    }

    String getArtifactId() {
        return exists ? pomXml.getProperty('artifactId') : ''
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId
    }

    Boolean doesExist() {
        return exists
    }

    String getPath() {
        return file.absolutePath
    }

    PomProperty getPomProperty(String propertyName) throws IllegalArgumentException {
        GPathResult p = pomProperties[propertyName] as GPathResult
        if (p == null) {
            throw new IllegalArgumentException('Property doesn\'t exist')
        }

        PomProperty prop = new PomProperty()
        prop.name = propertyName
        prop.value = p.text()
        prop.lineNo = parser.getNodeLineNumber(p)
        return prop
    }

    Integer getPropertiesLineNo() {
        return parser.getNodeLineNumber(pomProperties)
    }

    private GPathResult getPomProperties() {
        return pomXml[PROPERTIES] as GPathResult
    }

}
