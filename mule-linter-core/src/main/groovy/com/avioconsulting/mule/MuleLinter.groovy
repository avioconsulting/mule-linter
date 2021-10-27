package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []
    String  outputFormat

    MuleLinter(String applicationDirectory, String ruleConfigFile, String outputFormat) {
        this.app = new MuleApplication(new File(applicationDirectory))
        ruleSetList = parseConfigurationFile(ruleConfigFile)
        this.outputFormat= outputFormat
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
        RuleExecutor exe = this.buildLinterExecutor()

        // Display Results
        exe.displayResults(outputFormat,System.out)

    }

    @SuppressWarnings('UnnecessaryObjectReferences')
    RuleExecutor buildLinterExecutor() {

        // Create the executor
        RuleExecutor exe = new RuleExecutor(app, ruleSetList)

        // Execute
        exe.executeRules()

        return exe
    }

}
