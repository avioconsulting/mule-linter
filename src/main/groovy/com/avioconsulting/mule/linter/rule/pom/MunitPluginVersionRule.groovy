package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MunitPluginVersionRule extends PomPluginAttributeRule {

    static final String RULE_ID = 'MUNIT_PLUGIN_VERSION'
    static final String RULE_NAME = 'The Munit maven plugin contains the required version. '
    static final String GROUP_ID = 'com.mulesoft.munit.tools'
    static final String ARTIFACT_ID = 'munit-maven-plugin'

    MunitPluginVersionRule(String version) {
        super(RULE_ID, RULE_NAME, GROUP_ID, ARTIFACT_ID, ['version':version])
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }

}