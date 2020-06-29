package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.util.MavenUtil

class PomPropertyRule extends Rule{

    MavenUtil mavenUtil = new MavenUtil()

    PomPropertyRule(String ruleId, String ruleName) {
        super(ruleId, ruleName)
    }

    String findPropertyVersion(Application app, String propName){
        String propertyName = propName
        File pomFile = app.getPomFile().getFile()
        String property = mavenUtil.getMavenModel(pomFile).getProperties().getProperty(propertyName)

        return property
    }

}
