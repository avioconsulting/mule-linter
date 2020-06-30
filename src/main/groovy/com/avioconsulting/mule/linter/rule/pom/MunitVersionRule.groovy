package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation

class MunitVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MUNIT_VERSION'
    static final String PROPERTY_NAME = 'munit.version'
    static final String RULE_NAME = 'munit.version maven property match'

    String version

    MunitVersionRule(String version) {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME, version)
        this.version = version
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }
}
