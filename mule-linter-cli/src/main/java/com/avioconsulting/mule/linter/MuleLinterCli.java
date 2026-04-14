package com.avioconsulting.mule.linter;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.formatter.CompositeFormatter;
import com.avioconsulting.mule.linter.formatter.FormatterContext;
import com.avioconsulting.mule.linter.model.ReportFormat;
import com.avioconsulting.mule.linter.model.rule.RuleExecutor;
import com.avioconsulting.mule.linter.model.rule.RuleSeverity;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
            description = "Report Output Format(s). Multiple formats can be specified as comma-separated list. " +
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private String format;

    @CommandLine.Option(
            names = {"--output-dir"},
            defaultValue = "./target/mule-linter",
            description = "Output directory for file-based reports (JSON, XML)"
    )
    private File outputDirectory;

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
        // Parse comma-separated formats
        List<ReportFormat> formats = parseFormats(format);
        
        // Determine color usage
        boolean useColor = determineColorUsage();
        
        // Execute linter
        MuleLinter linter = new MuleLinter(appDir, ruleConfiguration);
        RuleExecutor executor = linter.execute();
        
        // Create formatter context
        FormatterContext context = new FormatterContext(
                outputDirectory,
                useColor,
                failThreshold,
                appDir
        );
        
        // Format results
        CompositeFormatter formatter = new CompositeFormatter(formats);
        formatter.format(executor, context);
        
        // Calculate and return exit code
        return CompositeFormatter.calculateExitCode(executor, failThreshold);
    }
    
    private List<ReportFormat> parseFormats(String formatString) {
        return Arrays.stream(formatString.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(ReportFormat::valueOf)
                .collect(Collectors.toList());
    }
    
    private boolean determineColorUsage() {
        if (noColor) {
            return false;
        }
        if (forceColor) {
            return true;
        }
        // Check NO_COLOR environment variable
        String noColorEnv = System.getenv("NO_COLOR");
        if (noColorEnv != null && !noColorEnv.isEmpty()) {
            return false;
        }
        // Auto-detect: use color by default
        return true;
    }
}
