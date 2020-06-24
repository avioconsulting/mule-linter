package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.ProjectFile
import com.avioconsulting.mule.linter.model.Rule

class PomExistsRule extends Rule {

//    PomExistsRule(ProjectFile file) {
//        super(file)
//    }

    PomExistsRule(Application app) {
        super("1", "Pom Exists", app)
    }

    @Override
    void execute() {
        // implement rule
        PomFile pfile = getApplication().getPomFile()
        if (pfile == null || !pfile.exists()) {
            raiseIssue(0, "pom.xml does not exist.")
        } else {
            System.out.println("Rule Pass.  pom.xml exists.")
        }

        // this should implment a 'raiseIssue' (or similar method)
    }
}
