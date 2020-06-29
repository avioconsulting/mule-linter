package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.model.RuleSeverity

class PomExistsRule extends Rule {
    static final String RULE_ID = "POM_EXISTS"
    static final String RULE_NAME = "pom.xml file exists"

//    PomExistsRule(ProjectFile file) {
//        super(file)
//    }

    PomExistsRule(Application app) {
        super(RULE_ID, RULE_NAME, app)
    }

    @Override
    List<RuleViolation> execute() {
        // implement rule
        PomFile pfile = getApplication().getPomFile()
        if (pfile == null || !pfile.exists()) {
//            raiseIssue(0, "pom.xml does not exist.")
            raiseIssue(RuleSeverity.BLOCKER, "pom.xml", 0, "File does not exist.")
        } else {
            println("Rule Pass. " + RULE_NAME)
        }
        return violations
    }

}
