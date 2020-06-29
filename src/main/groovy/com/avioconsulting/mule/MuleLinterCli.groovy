package com.avioconsulting.mule

import picocli.CommandLine

@CommandLine.Command(name = "MuleLinter", header = "%n@|green Mule Linter|@")
class MuleLinterCli implements Runnable {

    @CommandLine.Option(names = ["-r", "--rules"], required = true, description = "Rule definition file")
    String rules

    @CommandLine.Option(names = ["-d", "--dir"], required = true, description = "Application Directory")
    String appDir


    public static void main(String... args) {
        new CommandLine(new MuleLinterCli()).execute(args)
    }

    @Override
    void run() {
        // parse application to build out structure
        MuleLinter ml = new MuleLinter(appDir)
        ml.runLinter()
    }
}
