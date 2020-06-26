package com.avioconsulting.mule.linter.rule;

import com.avioconsulting.mule.linter.model.Application;
import com.avioconsulting.mule.linter.model.PomFile;
import com.avioconsulting.mule.linter.model.Rule
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.project.MavenProject;

class PomCheckProperties extends Rule {

    public PomCheckProperties(Application app) {
        super("1", "Pom Properties", app);
    }

    @Override
    public void execute() {
        getProjectVersion()
    }

    private static String getProjectVersion() {
        File corePomfile = new File( getApplication().getApplicationPath().getPath() + "/pom.xml");
        Model model = null;
        FileReader reader;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            reader = new FileReader(corePomfile);
            model = mavenReader.read(reader);
            model.setPomFile(corePomfile);
        } catch(Exception ex){

        }
        MavenProject project = new MavenProject(model);
        return project.getVersion();
    }
}