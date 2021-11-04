package com.avioconsulting.mule

import com.avioconsulting.mule.linter.dsl.Dsl
import com.avioconsulting.mule.linter.dsl.MuleLinterDsl
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet
import org.codehaus.groovy.control.CompilerConfiguration

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []
    String  outputFormat

    MuleLinter(String applicationDirectory, String ruleConfigFile, String outputFormat) {
        this.app = new MuleApplication(new File(applicationDirectory))
        //ruleSetList = parseConfigurationFile(ruleConfigFile)
        ruleSetList = processDSL(ruleConfigFile)
        this.outputFormat= outputFormat
    }

    List<RuleSet> processDSL(String ruleConfigFile){

        def compilerConfig = new CompilerConfiguration().with {
            scriptBaseClass = Dsl.name
            it
        }
        def binding = new Binding()
        binding.setVariable('params',[:])

        File groovyFile = new File(ruleConfigFile)

        def shell = new GroovyShell(
                this.class.classLoader,
                binding,
                compilerConfig
        )

        MuleLinterDsl ruleConfig = shell.evaluate(groovyFile) as MuleLinterDsl
        return [ruleConfig.getRules(ruleConfig.rulesDsl.ruleObj)]

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
