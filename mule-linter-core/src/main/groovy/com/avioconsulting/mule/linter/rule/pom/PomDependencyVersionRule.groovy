package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.Version
import com.avioconsulting.mule.linter.model.pom.PomDependency
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for the presence of a given dependency with a given version in the `pom.xml`.
 */
class PomDependencyVersionRule extends Rule {

    static final String RULE_ID = 'POM_DEPENDENCY_VERSION'
    static final String RULE_NAME = 'The given Maven dependency exists in pom.xml and matches given version criteria. '
    static final String MISSING_DEPENDENCY = 'Dependency does not exist: '
    static final String RULE_VIOLATION_MESSAGE = 'Dependency exist but invalid version: '
    Version version

    /**
     * groupId: is the group id for the jar in your artifact repository.
     * An example might be `"com.companyname"`.
     */
    @Param("groupId") String groupId

    /**
     * artifactId: is the artifact id for the jar in your artifact repository.
     * An example might be `"example-plugin"`.
     */
    @Param("artifactId") String artifactId

    /**
     * artifactVersion: is the artifact version for the jar in your artifact repository.
     * An example might be `"1.0.0-SNAPSHOT"`.
     */
    @Param("artifactVersion") String artifactVersion

    /**
     * versionOperator: is an Enum `Operator` found within class `com.avioconsulting.mule.linter.model.Version` describing how to match the provided `artifactVersion` to the one in the `pom.xml`.
     * Current options are `EQUAL` or `GREATER_THAN`.
     */
    @Param("versionOperator") String versionOperator

    private Version.Operator operator

    PomDependencyVersionRule(){
        super(RULE_ID, RULE_NAME)
        version= new Version()
        this.versionOperator = Version.Operator.EQUAL
    }

    PomDependencyVersionRule(String ruleId, String ruleName, String groupId, String artifactId, String versionOperator){
        super(ruleId, ruleName)
        this.groupId = groupId
        this.artifactId = artifactId
        this.versionOperator = versionOperator
        version= new Version()
    }
    @Override
    void init(){
        try{
            this.operator = Version.Operator.valueOf(versionOperator)
            version.setVersion(artifactVersion)
        }catch(Exception e){
            throw new IllegalArgumentException("Invalid value: '"+versionOperator+"'. Current options are: " + Version.Operator.values() )
        }
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomDependency dependency = app.pomFile.getDependency(groupId, artifactId)

        if ( dependency == null ) {
            violations.add(new RuleViolation(this, PomFile.POM_XML, 0, MISSING_DEPENDENCY + "$groupId , $artifactId"))
        } else {
            Boolean isViolated = false;
            PomElement attribute = dependency.getAttribute('version')
            String dependencyVersion = attribute.value
            switch (operator) {
                case Version.Operator.EQUAL:
                    isViolated = (!version.isEqual(dependencyVersion)) ? true : false
                    break;
                case Version.Operator.GREATER_THAN:
                    isViolated = (!version.isGreater(dependencyVersion)) ? true : false
            }
            if (isViolated) {
                violations.add(new RuleViolation(this, PomFile.POM_XML, 0,
                        RULE_VIOLATION_MESSAGE + "$groupId , $artifactId, $attribute.value"))
            }
        }
        return violations
    }

}

