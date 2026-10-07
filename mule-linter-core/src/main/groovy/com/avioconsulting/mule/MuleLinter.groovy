package com.avioconsulting.mule

import com.avioconsulting.mule.linter.dsl.ConfigurationLoader
import com.avioconsulting.mule.linter.dsl.MuleLinterDsl
import com.avioconsulting.mule.linter.dsl.RulesLoader
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.rule.cicd.JenkinsFileExistsRule

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []
    ReportFormat  outputFormat

    MuleLinter(File applicationDirectory, File ruleConfigFile, ReportFormat outputFormat) {
        // Validate configuration before application loading or parent repository access.
        ruleSetList = processDSL(ruleConfigFile)
        this.app = new MuleApplication(applicationDirectory)
        this.outputFormat= outputFormat
    }

    List<RuleSet> processDSL(File ruleConfigFile){

        MuleLinterDsl ruleConfig = ConfigurationLoader.load(ruleConfigFile)
        return [ruleConfig.rulesDsl.ruleSet]

    }

    List<RuleSet> parseConfigurationFile(File ruleConfigFile) {
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class dynamicRules = gcl.parseClass(ruleConfigFile)
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
