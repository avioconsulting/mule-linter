package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.VersionCompare
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomArtifact
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class PomDependencyVersionRule extends Rule {

    static final String RULE_ID = 'POM_DEPENDENCY_VERSION_CHECK'
    static final String RULE_NAME = 'Maven dependency matches exists pom.xml and matches to specified version criteria'
    static final String MISSING_DEPENDENCY = 'Dependency does not exits: '
    static final String RULE_VIOLATION_MESSAGE = 'Dependency exist but invalid version: '
    VersionCompare versionCompare = new VersionCompare()

    private final String groupId
    private final String artifactId
    private final String version
    private final VersionCompare.VersionOperator versionOperator
    private

    PomDependencyVersionRule(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, VersionCompare.VersionOperator.EQUAL)
    }

    PomDependencyVersionRule(String groupId, String artifactId, String version,
                             VersionCompare.VersionOperator versionOperator) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
        this.versionOperator = versionOperator
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomArtifact plugin = app.pomFile.getDependency(groupId, artifactId)

        if ( plugin == null ) {
            violations.add(new RuleViolation(this, app.pomFile.path, 0, MISSING_DEPENDENCY + "$groupId , $artifactId"))
        } else {
            PomElement attribute = plugin.getAttribute('version')
            if ( !versionCompare.isValidVersion(version, attribute.value, versionOperator)) {
                violations.add(new RuleViolation(this, app.pomFile.path, attribute.lineNo,
                        RULE_VIOLATION_MESSAGE + "$groupId , $artifactId, $attribute.value"))
            }
        }
        return violations
    }

}

