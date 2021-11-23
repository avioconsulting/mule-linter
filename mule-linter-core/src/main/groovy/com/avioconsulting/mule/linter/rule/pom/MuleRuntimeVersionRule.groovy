package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MuleRuntimeVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MULE_RUNTIME'
    static final String RULE_NAME = 'The app.runtime maven property matches the given version. '
    static final String PROPERTY_NAME = 'app.runtime'

    @Param("version") String version

    MuleRuntimeVersionRule() {
        super(RULE_ID, RULE_NAME, PROPERTY_NAME)
    }

    MuleRuntimeVersionRule(String version) {
        this()
        this.version = version
        init()
    }

    @Override
    void init(){
        if(version != null)
            this.propertyValue = version
        else
            throw new NoSuchFieldException("version")
    }

    @Override
    List<RuleViolation> execute(Application app) {
        return super.execute(app)
    }

}
