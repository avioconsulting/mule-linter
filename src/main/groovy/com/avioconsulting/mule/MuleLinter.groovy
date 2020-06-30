package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleExecuter
import com.avioconsulting.mule.linter.model.RuleSet
import com.avioconsulting.mule.linter.rule.PomExistsRule

class MuleLinter {

    Application app

    MuleLinter(String applicationDirectory) {
        this.app = new Application(new File(applicationDirectory))
    }

    void runLinter() {
        // Build a list of rules
        RuleSet rules = new RuleSet();
        rules.addRule(new PomExistsRule())

        // Create the executor
        RuleExecuter exe = new RuleExecuter(app, rules)

        // Execute
        exe.executeRules()

        // Display Results
        exe.displayResults()
    }
}
