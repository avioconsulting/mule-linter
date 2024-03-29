package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for the existence of a `pom.xml` file at the root of the project.
 * Maven is required in Mule 4, and your project won't build if you don't have a `pom.xml`, so this rule might not be strictly necessary for a project.
 */
class PomExistsRule extends Rule {

    static final String RULE_ID = 'POM_FILE_EXISTS'
    static final String RULE_NAME = 'The pom.xml file exists. '
    static final String FILE_NOT_EXISTS = 'The pom.xml does not exist'

    PomExistsRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomFile pomFile = app.pomFile
        if (pomFile == null || !pomFile.doesExist()) {
            violations.add(new RuleViolation(this, app.pomFile.file.toString(), 0, FILE_NOT_EXISTS))
        }
        return violations
    }

}
