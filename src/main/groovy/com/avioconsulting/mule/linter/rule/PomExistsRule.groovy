package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.ProjectFile
import com.avioconsulting.mule.linter.model.Rule

class PomExistsRule extends Rule {

//    PomExistsRule(ProjectFile file) {
//        super(file)
//    }

    PomExistsRule(ProjectFile file) {
        super("1", "Pom Exists", file)
    }

    @Override
    void execute() {
        // implement rule
        PomFile pfile = (PomFile) getProjectFile()
        if (pfile == null || !pfile.exists()) {
            raiseIssue(0, "pom.xml does not exist.")
        } else {
            System.out.println("Rule Pass.  pom.xml exists.")
        }

        // this should implment a 'raiseIssue' (or similar method)
    }
}
