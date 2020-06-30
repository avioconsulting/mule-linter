package com.avioconsulting.mule.linter.util

import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader

class MavenUtil {

    private Model getMavenModel(File pomFile) {
        Model model = null;
        FileReader reader;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(pomFile)
            model = mavenReader.read(reader)
            model.pomFile = pomFile
        } catch(Exception ex){

        }

        return model
    }
}
