package com.avioconsulting.mule

import com.avioconsulting.mule.linter.dsl.Dsl
import com.avioconsulting.mule.linter.dsl.MuleLinterDsl
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Main linter class that loads rules and executes them against a Mule application.
 * Formatting and exit codes are handled by the caller (CLI or Maven plugin).
 */
@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []

    /**
     * Create a new MuleLinter for the given application and rules file.
     */
    MuleLinter(File applicationDirectory, File ruleConfigFile) {
        this.app = new MuleApplication(applicationDirectory)
        this.ruleSetList = processDSL(ruleConfigFile)
    }

    /**
     * Process the rules DSL file and return the configured rule sets.
     */
    List<RuleSet> processDSL(File ruleConfigFile) {
        def compilerConfig = new CompilerConfiguration().with {
            scriptBaseClass = Dsl.name
            it
        }
        def binding = new Binding()
        binding.setVariable('params', [:])

        def shell = new GroovyShell(
                this.class.classLoader,
                binding,
                compilerConfig
        )

        MuleLinterDsl ruleConfig = shell.evaluate(ruleConfigFile) as MuleLinterDsl
        return [ruleConfig.rulesDsl.ruleSet]
    }

    /**
     * Execute all rules against the application.
     * 
     * @return RuleExecutor containing all results
     */
    RuleExecutor execute() {
        RuleExecutor executor = new RuleExecutor(app, ruleSetList)
        executor.executeRules()
        return executor
    }
}
