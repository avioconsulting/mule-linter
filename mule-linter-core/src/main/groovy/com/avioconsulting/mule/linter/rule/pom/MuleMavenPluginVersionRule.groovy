package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MuleMavenPluginVersionRule extends PomPluginAttributeRule {

    static final String RULE_ID = 'MULE_MAVEN_PLUGIN'
    static final String RULE_NAME = 'The mule maven plugin is a valid version. '
    static final String GROUP_ID = 'org.mule.tools.maven'
    static final String ARTIFACT_ID = 'mule-maven-plugin'

    @Param("version") String version

    MuleMavenPluginVersionRule(){
        super(RULE_ID, RULE_NAME, GROUP_ID, ARTIFACT_ID)
    }

    @Override
    void init(){
        if(version != null)
            this.attributes = ['version':version]
        else
            throw new NoSuchFieldException("version")
    }
}
