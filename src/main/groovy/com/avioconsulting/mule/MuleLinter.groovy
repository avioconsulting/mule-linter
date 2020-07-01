package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleExecutor
import com.avioconsulting.mule.linter.model.RuleSet
import com.avioconsulting.mule.linter.rule.pom.MuleMavenPluginVersionRule
import com.avioconsulting.mule.linter.rule.pom.MunitVersionRule
import com.avioconsulting.mule.linter.rule.pom.MuleRuntimeVersionRule
import com.avioconsulting.mule.linter.rule.pom.PomExistsRule
import com.avioconsulting.mule.linter.rule.property.PropertyFileNamingRule
import com.avioconsulting.mule.linter.rule.property.PropertyFilePropertyCountRule

class MuleLinter {

    Application app

    MuleLinter(String applicationDirectory) {
        this.app = new Application(new File(applicationDirectory))
    }

    void runLinter() {
        // Build a list of rules
        RuleSet rules = new RuleSet()
        rules.addRule(new PomExistsRule())
        rules.addRule(new MuleMavenPluginVersionRule('3.3.5'))
        rules.addRule(new MunitVersionRule('2.2.1'))
        rules.addRule(new MuleRuntimeVersionRule('4.2.1'))
        rules.addRule(new PropertyFileNamingRule(['dev', 'test']))
        rules.addRule(new PropertyFilePropertyCountRule(['dev', 'test', 'uat', 'prod'], '${env}.properties'))

        // Create the executor
        RuleExecutor exe = new RuleExecutor(app, rules)

        // Execute
        exe.executeRules()

        // Display Results
        exe.displayResults(System.out)
    }

}
