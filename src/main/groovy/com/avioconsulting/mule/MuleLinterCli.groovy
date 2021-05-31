package com.avioconsulting.mule

import picocli.CommandLine

@CommandLine.Command(name = 'MuleLinter', header = '%n@|green Mule Linter|@')
class MuleLinterCli implements Runnable {

    @CommandLine.Option(names = ['-r', '--rules'], required = true, description = 'Rule configuration file')
    String ruleConfiguration

    @CommandLine.Option(names = ['-d', '--dir'], required = true, description = 'Application Directory')
    String appDir

    @CommandLine.Option(names = ['-f', '--format'], description = 'required output')
    String outputFormat

    static void main(String... args) {
        new CommandLine(new MuleLinterCli()).execute(args)
    }

    @Override
    void run() {
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, outputFormat)
        ml.runLinter()
    }

}
