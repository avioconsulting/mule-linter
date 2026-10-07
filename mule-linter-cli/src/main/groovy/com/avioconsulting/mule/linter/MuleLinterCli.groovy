package com.avioconsulting.mule.linter

import com.avioconsulting.mule.MuleLinter
import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.model.rule.FailurePolicy
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.ReportWriters
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = 'mule-linter', mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 2,
        footer = '\nCopyright: 2021 AVIO Consulting, License: MIT\nWebsite: https://github.com/avioconsulting/mule-linter',
        description = 'Analyze mule application code for patterns that don’t follow convention', showDefaultValues = true,
        header = '%n@|green Mule Linter|@')
class MuleLinterCli implements Callable<Integer>, Runnable {

    @CommandLine.Option(names = ['-r', '--rules'], required = true, description = 'Rule configuration file')
    File ruleConfiguration

    @CommandLine.Option(names = ['-d', '--dir'], required = true, description = 'Application Directory')
    File appDir

    @CommandLine.Option(names = ['-f', '--format'], defaultValue = 'CONSOLE',
            description = 'Report Output Format. Valid values: ${COMPLETION-CANDIDATES}')
    ReportFormat outputFormat

    @CommandLine.Option(names = '--fail', description = 'Fail on findings at or above the threshold')
    boolean fail

    @CommandLine.Option(names = '--threshold', defaultValue = 'MAJOR',
            description = 'Failure severity threshold. Valid values: ${COMPLETION-CANDIDATES}')
    RuleSeverity threshold = RuleSeverity.MAJOR

    @CommandLine.Option(names = '--strict', description = 'Fail if analysis is incomplete (for example unresolved parents)')
    boolean strict

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec

    static void main(String... args) {
        System.exit(new CommandLine(new MuleLinterCli()).execute(args))
    }

    @Override
    Integer call() {
        PrintWriter errors = spec == null ? new PrintWriter(System.err, true) : spec.commandLine().err
        try {
            MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat)
            def result = ml.buildLinterExecutor().analysisResult
            // Do not close System.out: the harness or caller owns it.
            ReportWriters.write(result, outputFormat, System.out)
            if (System.out.checkError()) throw new IOException('Failed to write report')
            if (outputFormat != ReportFormat.CONSOLE) {
                result.warnings.each { errors.println("Analysis warning: $it") }
                if (!result.complete) errors.println('Analysis incomplete.')
            }
            FailurePolicy.requireComplete(result, strict)
            return FailurePolicy.fails(result, fail, threshold) ? 1 : 0
        } catch (Exception e) {
            errors.println("Linter error: ${e.message}")
            return 2
        }
    }

    /** Compatibility entrypoint; callers needing status should use call() or Picocli execute(). */
    @Deprecated
    @Override
    void run() { call() }

}
