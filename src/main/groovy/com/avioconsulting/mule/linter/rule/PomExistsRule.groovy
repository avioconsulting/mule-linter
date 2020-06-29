package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PomFile
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.model.RuleSeverity

class PomExistsRule extends Rule {
    static final String RULE_ID = "POM_EXISTS"
    static final String RULE_NAME = "pom.xml file exists"
    static final String FILE_NOT_EXISTS = "File does not exist."

    PomExistsRule(Application app) {
        super(RULE_ID, RULE_NAME, app)
    }

    @Override
    List<RuleViolation> execute() {
        List<RuleViolation> violations = new ArrayList<RuleViolation>()

        // implement rule
        PomFile pfile = getApplication().getPomFile()
        if (pfile == null || !pfile.exists()) {
            violations.add(new RuleViolation(this, pfile.getName(), 0, FILE_NOT_EXISTS))
        }
        return violations
    }

}
