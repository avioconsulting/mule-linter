package com.avioconsulting.mule.linter.model

import groovy.xml.slurpersupport.GPathResult
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException

class PomFile extends ProjectFile {

//    private Model model
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
        }
    }

    PomFile(File application, String fileName) {
        this(new File(application, fileName))
    }

    Boolean doesExist() {
        return exists
    }

    String getPath() {
        return file.absolutePath
    }

    PomProperty getPomProperty(String propertyName) throws IllegalArgumentException {
//        return exists ? model?.properties?.getProperty(propertyName) : ''
        GPathResult p = pomXml['properties'][propertyName]
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
        GPathResult props = pomXml['properties']
        return parser.getNodeLineNumber(props)
    }

//    private static Model parseModel(File pomFile) {
//        Model model = null
//        FileReader reader
//        MavenXpp3Reader mavenReader = new MavenXpp3Reader()
//        try {
//            reader = new FileReader(pomFile)
//            model = mavenReader.read(reader)
//            model.pomFile = pomFile
//        } catch (IOException | XmlPullParserException ex) {
//            ex.printStackTrace()
//        }
//
//        return model
//    }

}
