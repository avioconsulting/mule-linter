package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleExecuter
import com.avioconsulting.mule.linter.model.RuleSeverity
import com.avioconsulting.mule.linter.rule.PomCheckProperties
import com.avioconsulting.mule.linter.rule.PomExistsRule

class MuleLinter {
    Application app

    MuleLinter(String applicationDirectory){
        File appDir = new File(applicationDirectory)
        Application app = new Application(appDir)
        this.app = app
    }

//    public static void main(String[] args) {
//        MuleLinterCli.main(args)
//    }


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
