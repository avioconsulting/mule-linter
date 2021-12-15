package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that the version number for the `munit-maven-version` plugin matches a given value.
 */
class MunitPluginVersionRule extends PomPluginAttributeRule {

    static final String RULE_ID = 'MUNIT_PLUGIN_VERSION'
    static final String RULE_NAME = 'The Munit maven plugin contains the required version. '
    static final String GROUP_ID = 'com.mulesoft.munit.tools'
    static final String ARTIFACT_ID = 'munit-maven-plugin'

    /**
     * version: is the version number expected for the `munit-maven-version` plugin.
     */
    @Param("version") String version

    MunitPluginVersionRule() {
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