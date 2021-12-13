package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.rule.Param

/**
 * This rule ensures that the `app.runtime` property within the `pom.xml` matches a given value.
 */
class MuleRuntimeVersionRule extends PomPropertyValueRule {

    static final String RULE_ID = 'MULE_RUNTIME'
    static final String RULE_NAME = 'The app.runtime maven property matches the given version. '
    static final String PROPERTY_NAME = 'app.runtime'

    /**
     * *version* is the version number for the property `app.runtime` that is expected within the `pom.xml`.
     */
    @Param("version") String version

    MuleRuntimeVersionRule() {
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
