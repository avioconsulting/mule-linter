package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MunitVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MUNIT_VERSION'
    static final String RULE_NAME = 'The munit.version maven property matches the given version. '
    static final String PROPERTY_NAME = 'munit.version'

    @Param("version") String version

    MunitVersionRule() {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME)
    }

    @Override
    void init(){
        if(version != null)
            this.propertyValue = version
        else
            throw new NoSuchFieldException("version")
    }
}
