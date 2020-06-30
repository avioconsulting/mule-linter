package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation

class MuleRuntimeVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MULE_RUNTIME'
    static final String PROPERTY_NAME = 'app.runtime'
    static final String RULE_NAME = 'app.runtime maven property match'

    MuleRuntimeVersionRule(String version) {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME, version)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }

}
