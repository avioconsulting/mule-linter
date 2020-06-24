package com.avioconsulting.mule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.rule.PomExistsRule

class MuleLinter {
    Application app

    MuleLinter(Application app){
        this.app = app
    }

//    public static void main(String[] args) {
//        MuleLinterCli.main(args)
//    }


    public void runLinter() {

        Rule r1 = new PomExistsRule(app.getPomFile())
        r1.setSeverity("SUPER_IMPORTANT")
        r1.execute()

        // Parse rule configuration, and add rules to array
        // ultimately add rule to an array of rules, and execute them all sequentially
    }
}
