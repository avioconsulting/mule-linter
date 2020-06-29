package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleExecuter
import com.avioconsulting.mule.linter.rule.PomExistsRule

class MuleLinter {
    Application app

    MuleLinter(String applicationDirectory){
        this.app = new Application(new File(applicationDirectory))
    }

    public void runLinter() {
        // Create the executer
        RuleExecuter exe = new RuleExecuter()

        // Add ALL rules to run
        exe.addRule(new PomExistsRule(app))

        // Execute
        exe.executeRules()

        // Display Results
        exe.displayResults()
    }
}
