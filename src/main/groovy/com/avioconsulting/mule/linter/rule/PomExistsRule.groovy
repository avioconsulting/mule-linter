package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class PomExistsRule extends Rule {

    static final String RULE_ID = 'POM_EXISTS'
    static final String RULE_NAME = 'pom.xml file exists'
    static final String POM_FILE = 'pom.xml'
    static final String FILE_NOT_EXISTS = 'File does not exist.'

    PomExistsRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomFile pomFile = app.pomFile
        if (pomFile == null || !pomFile.exists()) {
            violations.add(new RuleViolation(this, POM_FILE, 0, FILE_NOT_EXISTS))
        }
        return violations
    }

}
