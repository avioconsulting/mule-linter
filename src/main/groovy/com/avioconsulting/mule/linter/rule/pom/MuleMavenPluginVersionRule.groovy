package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation

class MuleMavenPluginVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MULE_MAVEN_PLUGIN'
    static final String PROPERTY_NAME = 'mule.maven.plugin.version'
    static final String RULE_NAME = 'mule.maven.plugin.version maven property match'

    MuleMavenPluginVersionRule(String version) {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME, version)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }

}
