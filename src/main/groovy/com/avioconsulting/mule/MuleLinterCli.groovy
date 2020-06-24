package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.rule.PomExistsRule
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
        File appDir = new File(appDir)
        Application app = new Application(appDir)

        MuleLinter ml = new MuleLinter(app)
        ml.runLinter()
//
//        Rule r1 = new PomExistsRule(app.getPomFile())
//        r1.setSeverity("SUPER_IMPORTANT")
//        r1.execute()
//
//        // Parse rule configuration, and add rules to array
//        // ultimately add rule to an array of rules, and execute them all sequentially


//        System.out.println(app.hasGitIgnore())
//        System.out.println(app.hasFile('test/file.txt'))
//        System.out.println(app.hasFile('test/file2.txt'))
    }
}
