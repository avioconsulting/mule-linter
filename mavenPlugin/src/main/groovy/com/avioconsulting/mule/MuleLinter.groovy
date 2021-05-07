package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []

    MuleLinter(String applicationDirectory, String ruleConfigFile) {
        this.app = new Application(new File(applicationDirectory))
        ruleSetList = parseConfigurationFile(ruleConfigFile)
    }

    List<RuleSet> parseConfigurationFile(String ruleConfigFile) {
        GroovyClassLoader gcl = new GroovyClassLoader()
        File config = new File(ruleConfigFile)
        Class dynamicRules = gcl.parseClass(config)
        RuleSet rules = dynamicRules.getRules()
        return [rules]
    }

    @SuppressWarnings('UnnecessaryObjectReferences')
    void runLinter() {
        // Create the executor
        RuleExecutor exe = new RuleExecutor(app, ruleSetList)

        // Execute
        exe.executeRules()

        // Display Results
        exe.displayResults(System.out)
    }

}
