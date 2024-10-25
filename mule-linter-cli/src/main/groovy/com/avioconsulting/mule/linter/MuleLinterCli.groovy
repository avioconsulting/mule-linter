package com.avioconsulting.mule.linter

import com.avioconsulting.mule.MuleLinter
import com.avioconsulting.mule.linter.model.ReportFormat
import picocli.CommandLine

@CommandLine.Command(name = 'mule-linter', mixinStandardHelpOptions = true,
        footer = '\nCopyright: 2021 AVIO Consulting, License: MIT\nWebsite: https://github.com/avioconsulting/mule-linter',
        description = 'Analyze mule application code for patterns that don’t follow convention', showDefaultValues = true,
        header = '%n@|green Mule Linter|@')
class MuleLinterCli implements Runnable {

    @CommandLine.Option(names = ['-r', '--rules'], required = true, description = 'Rule configuration file')
    File ruleConfiguration

    @CommandLine.Option(names = ['-d', '--dir'], required = true, description = 'Application Directory')
    File appDir

    @CommandLine.Option(names = ['-f', '--format'], defaultValue = 'CONSOLE',
            description = 'Report Output Format. Valid values: ${COMPLETION-CANDIDATES}')
    ReportFormat outputFormat

    @CommandLine.Option(names = ['-p', '--profiles'], arity = '0..*', description = 'List of Maven profiles to apply', split = ',')
    List<String> profiles

    static void main(String... args) {
        new CommandLine(new MuleLinterCli()).execute(args)
    }

    @Override
    void run() {
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat, profiles)
        ml.runLinter()
    }

}