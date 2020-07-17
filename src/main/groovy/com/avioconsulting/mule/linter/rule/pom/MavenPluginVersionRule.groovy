package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomPlugin
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class MavenPluginVersionRule extends Rule {

    static final String RULE_ID = 'MAVEN_PLUGIN_VERSION'
    static final String RULE_NAME = 'Maven plugin exists in pom.xml and matches the version.'
    static final String MISSING_PLUGIN = 'Plugin does not exits: '
    static final String RULE_VIOLATION_MESSAGE = 'Plugin exist but does not matches the version: '

    private final String groupId
    private final String artifactId
    private final String version

    MavenPluginVersionRule(String groupId, String artifactId, String version) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
    }

    MavenPluginVersionRule(String ruleId, String ruleName, String groupId, String artifactId, String version) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomPlugin plugin = app.pomFile.getPlugin(groupId, artifactId)

        if ( plugin == null ) {
            violations.add(new RuleViolation(this, app.pomFile.path, 0, MISSING_PLUGIN + "$groupId , $artifactId"))
        } else {
            if ( plugin.version.value != version) {
                violations.add(new RuleViolation(this, app.pomFile.path, plugin.version.lineNo,
                                RULE_VIOLATION_MESSAGE + version))
            }
        }
        return violations
    }

}

