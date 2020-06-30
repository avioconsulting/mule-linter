package com.avioconsulting.mule.linter.model

import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException

class PomFile extends ProjectFile {

    private Model model
    private final Boolean exists

    PomFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
            model = parseModel(file)
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

    String getProperty(String propertyName) {
        return exists ? model?.properties?.getProperty(propertyName) : ''
    }

    private static Model parseModel(File pomFile) {
        Model model = null
        FileReader reader
        MavenXpp3Reader mavenReader = new MavenXpp3Reader()
        try {
            reader = new FileReader(pomFile)
            model = mavenReader.read(reader)
            model.pomFile = pomFile
        } catch (IOException | XmlPullParserException ex) {
            ex.printStackTrace()
        }

        return model
    }

}
