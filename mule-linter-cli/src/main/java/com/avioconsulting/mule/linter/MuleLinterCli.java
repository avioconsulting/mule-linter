package com.avioconsulting.mule.linter;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.model.ReportFormat;
import com.avioconsulting.mule.linter.model.rule.RuleSeverity;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "mule-linter",
        mixinStandardHelpOptions = true,
        footer = "\nCopyright: 2021 AVIO Consulting, License: MIT\nWebsite: https://github.com/avioconsulting/mule-linter",
        description = "Analyze mule application code for patterns that don't follow convention",
        showDefaultValues = true,
        header = "\n@|green Mule Linter|@"
)
public class MuleLinterCli implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-r", "--rules"},
            required = true,
            description = "Rule configuration file"
    )
    private File ruleConfiguration;

    @CommandLine.Option(
            names = {"-d", "--dir"},
            required = true,
            description = "Application Directory"
    )
    private File appDir;

    @CommandLine.Option(
            names = {"-f", "--format"},
            defaultValue = "CONSOLE",
            description = "Report Output Format. Valid values: ${COMPLETION-CANDIDATES}"
    )
    private ReportFormat outputFormat;

    @CommandLine.Option(
            names = {"--fail-threshold"},
            defaultValue = "MAJOR",
            description = "Minimum severity that causes a non-zero exit code. " +
                    "Violations below this threshold will be reported but won't fail the build."
    )
    private RuleSeverity failThreshold;

    @CommandLine.Option(
            names = {"--color"},
            description = "Force ANSI color output even in non-interactive environments"
    )
    private boolean forceColor = false;

    @CommandLine.Option(
            names = {"--no-color"},
            description = "Disable ANSI color output"
    )
    private boolean noColor = false;

    public static void main(String... args) {
        int exitCode = new CommandLine(new MuleLinterCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        boolean useColor = determineColorUsage();
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat, failThreshold, useColor);
        int exitCode = ml.runLinter();
        return exitCode;
    }

    private boolean determineColorUsage() {
        if (noColor) {
            return false;
        }
        if (forceColor) {
            return true;
        }
        // Auto-detect: picocli handles this via Ansi.AUTO
        // We'll pass null/undefined and let the Groovy code use picocli's Ansi class
        return true; // Default, actual detection happens in RuleExecutor
    }
}
