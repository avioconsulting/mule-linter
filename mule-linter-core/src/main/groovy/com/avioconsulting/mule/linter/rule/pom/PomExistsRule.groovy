package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.FileExistsRule

/**
 * This rule checks for the existence of a `pom.xml` file at the root of the project.
 * Maven is required in Mule 4, and your project won't build if you don't have a `pom.xml`, so this rule might not be strictly necessary for a project.
 */
class PomExistsRule extends FileExistsRule {

    static final String RULE_ID = 'POM_FILE_EXISTS'
    static final String RULE_NAME = 'The pom.xml file exists. '
    static final String FILE_NOT_EXISTS = 'The pom.xml does not exist'

    PomExistsRule() {
        super(RULE_ID, RULE_NAME,  PomFile.POM_XML, FILE_NOT_EXISTS)
    }

}
