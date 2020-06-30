package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleExecuter
import com.avioconsulting.mule.linter.model.RuleSet
import com.avioconsulting.mule.linter.rule.PomCheckMulePluginRule
import com.avioconsulting.mule.linter.rule.PomCheckMunitRule
import com.avioconsulting.mule.linter.rule.PomCheckRuntimeRule
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
        rules.addRule(new PomCheckMulePluginRule("3.3.5"))
        rules.addRule(new PomCheckMunitRule("2.2.1"))
        rules.addRule(new PomCheckRuntimeRule("4.2.1"))

        // Create the executor
        RuleExecuter exe = new RuleExecuter(app, rules)

        // Execute
        exe.executeRules()

        // Display Results
        exe.displayResults()
    }
}
