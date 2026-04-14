package com.avioconsulting.mule.linter;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.model.ReportFormat;
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

    public static void main(String... args) {
        int exitCode = new CommandLine(new MuleLinterCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat);
        ml.runLinter();
        return 0;
    }
}
