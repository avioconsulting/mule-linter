package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Version
import com.avioconsulting.mule.linter.model.pom.PomDependency
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class PomDependencyVersionRule extends Rule {

    static final String RULE_ID = 'POM_DEPENDENCY_VERSION'
    static final String RULE_NAME = 'The given Maven dependency exists in pom.xml and matches given version criteria. '
    static final String MISSING_DEPENDENCY = 'Dependency does not exist: '
    static final String RULE_VIOLATION_MESSAGE = 'Dependency exist but invalid version: '
    Version version= new Version()

    private final String groupId
    private final String artifactId
    private final String artifactVersion
    private final Version.Operator versionOperator

    PomDependencyVersionRule(String groupId, String artifactId, String artifactVersion) {
        this(groupId, artifactId, artifactVersion, Version.Operator.EQUAL)
    }

    PomDependencyVersionRule(String groupId, String artifactId, String artifactVersion,
                             Version.Operator versionOperator) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.groupId = groupId
        this.artifactId = artifactId
        this.artifactVersion = artifactVersion
        this.versionOperator = versionOperator
        version.setVersion(artifactVersion)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomDependency dependency = app.pomFile.getDependency(groupId, artifactId)

        if ( dependency == null ) {
            violations.add(new RuleViolation(this, app.pomFile.path, 0, MISSING_DEPENDENCY + "$groupId , $artifactId"))
        } else {
            Boolean isViolated = false;
            PomElement attribute = dependency.getAttribute('version')
            String dependencyVersion = attribute.value
            switch (versionOperator) {
                case Version.Operator.EQUAL:
                    isViolated = (!version.isEqual(dependencyVersion)) ? true : false
                    break;
                case Version.Operator.GREATER_THAN:
                    isViolated = (!version.isGreater(dependencyVersion)) ? true : false
            }
            if (isViolated) {
                violations.add(new RuleViolation(this, app.pomFile.path, attribute.lineNo,
                        RULE_VIOLATION_MESSAGE + "$groupId , $artifactId, $attribute.value"))
            }
        }
        return violations
    }

}

