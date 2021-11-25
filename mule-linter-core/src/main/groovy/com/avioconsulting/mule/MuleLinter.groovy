package com.avioconsulting.mule

import com.avioconsulting.mule.linter.dsl.Dsl
import com.avioconsulting.mule.linter.dsl.MuleLinterDsl
import com.avioconsulting.mule.linter.dsl.RulesLoader
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.rule.cicd.JenkinsFileExistsRule
import org.codehaus.groovy.control.CompilerConfiguration

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []
    ReportFormat  outputFormat

    MuleLinter(File applicationDirectory, File ruleConfigFile, ReportFormat outputFormat) {
        this.app = new MuleApplication(applicationDirectory)
        //ruleSetList = parseConfigurationFile(ruleConfigFile)
        ruleSetList = processDSL(ruleConfigFile)
        this.outputFormat= outputFormat
    }
    List<RuleSet> processDSL(File ruleConfigFile){

        def compilerConfig = new CompilerConfiguration().with {
            scriptBaseClass = Dsl.name
            it
        }
        def binding = new Binding()
        binding.setVariable('params',[:])

        def shell = new GroovyShell(
                this.class.classLoader,
                binding,
                compilerConfig
        )

        MuleLinterDsl ruleConfig = shell.evaluate(ruleConfigFile) as MuleLinterDsl
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
