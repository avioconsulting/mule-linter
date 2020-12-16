package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MunitVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MUNIT_VERSION'
    static final String RULE_NAME = 'The munit.version maven property matches the given version. '
    static final String PROPERTY_NAME = 'munit.version'

    MunitVersionRule(String version) {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME, version)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }

}
