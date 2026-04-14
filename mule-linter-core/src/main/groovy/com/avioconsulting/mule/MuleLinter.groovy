package com.avioconsulting.mule

import com.avioconsulting.mule.linter.dsl.Dsl
import com.avioconsulting.mule.linter.dsl.MuleLinterDsl
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.RuleExecutor
import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import org.codehaus.groovy.control.CompilerConfiguration

@SuppressWarnings(['All', 'GStringExpressionWithinString'])
class MuleLinter {

    Application app
    List<RuleSet> ruleSetList = []
    ReportFormat outputFormat
    RuleSeverity failThreshold
    boolean useColor

    /**
     * Legacy constructor for backward compatibility.
     * Uses default threshold of MAJOR and auto-detects colors.
     */
    MuleLinter(File applicationDirectory, File ruleConfigFile, ReportFormat outputFormat) {
        this(applicationDirectory, ruleConfigFile, outputFormat, RuleSeverity.MAJOR, true)
    }

    /**
     * Full constructor with exit code threshold and color support.
     */
    MuleLinter(File applicationDirectory, File ruleConfigFile, ReportFormat outputFormat,
               RuleSeverity failThreshold, boolean useColor) {
        this.app = new MuleApplication(applicationDirectory)
        this.ruleSetList = processDSL(ruleConfigFile)
        this.outputFormat = outputFormat
        this.failThreshold = failThreshold
        this.useColor = useColor
    }

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

    List<RuleSet> parseConfigurationFile(File ruleConfigFile) {
        GroovyClassLoader gcl = new GroovyClassLoader()
        Class dynamicRules = gcl.parseClass(ruleConfigFile)
        RuleSet rules = dynamicRules.getRules()
        return [rules]
    }

    /**
     * Run the linter and display results.
     * @return exit code: 0 for success (no violations or below threshold), 1 for violations at/above threshold
     */
    @SuppressWarnings('UnnecessaryObjectReferences')
    int runLinter() {
        RuleExecutor exe = this.buildLinterExecutor()
        exe.displayResults(outputFormat, System.out, useColor)
        return calculateExitCode(exe.results, failThreshold)
    }

    @SuppressWarnings('UnnecessaryObjectReferences')
    RuleExecutor buildLinterExecutor() {
        RuleExecutor exe = new RuleExecutor(app, ruleSetList)
        exe.executeRules()
        return exe
    }

    /**
     * Calculate exit code based on violations and threshold.
     * @param violations list of all violations found
     * @param threshold minimum severity that causes failure
     * @return 0 if no violations at/above threshold, 1 otherwise
     */
    private int calculateExitCode(List<RuleViolation> violations, RuleSeverity threshold) {
        boolean hasFailing = violations.any { violation ->
            violation.rule.severity.ordinal() <= threshold.ordinal()
        }
        return hasFailing ? 1 : 0
    }
}
