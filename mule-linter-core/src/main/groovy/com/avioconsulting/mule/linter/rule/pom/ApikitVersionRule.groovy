package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Version
import com.avioconsulting.mule.linter.model.pom.PomDependency
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ApikitVersionRule extends PomDependencyVersionRule {
    static final String RULE_ID = 'APIKIT_VERSION'
    static final String RULE_NAME = 'The given Maven dependency exists in pom.xml and matches given version criteria. '

    static final String GROUP_ID = 'org.mule.modules'
    static final String ARTIFACT_ID = 'mule-apikit-module'
    static final String ARTIFACT_VERSION = '1.9.0'
    static final String VERSION_OPERATOR = Version.Operator.GREATER_THAN

    /**
     * artifactVersion: is the artifact version for the APIKit module in your artifact repository.
     * This is currently default to 1.9.0, and can be overriden by passing in the rule.
     */
    @Param("artifactVersion") String artifactVersion

    ApikitVersionRule(){
        super(RULE_ID, RULE_NAME,GROUP_ID,ARTIFACT_ID,VERSION_OPERATOR)
    }

    @Override
    void init(){
        if(artifactVersion != null)
            super.artifactVersion = artifactVersion
        else
            super.artifactVersion = ARTIFACT_VERSION
        super.init()
    }

}
