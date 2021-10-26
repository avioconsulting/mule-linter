package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomPlugin
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class PomPluginAttributeRule extends Rule {

    static final String RULE_ID = 'POM_PLUGIN_ATTRIBUTE'
    static final String RULE_NAME = 'The given Maven plugin exists in the pom.xml and matches the given attributes. '
    static final String MISSING_PLUGIN = 'Plugin does not exits: '
    static final String RULE_VIOLATION_MESSAGE = 'Plugin exist but does not matches the attribute: '

    private final String groupId
    private final String artifactId
    private final Map<String,String> attributes

    PomPluginAttributeRule(String groupId, String artifactId, Map<String,String> attributes) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.groupId = groupId
        this.artifactId = artifactId
        this.attributes = attributes
    }

    PomPluginAttributeRule(String ruleId, String ruleName, String groupId, String artifactId, Map<String,String> attributes) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.groupId = groupId
        this.artifactId = artifactId
        this.attributes = attributes
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        PomPlugin plugin = app.pomFile.getPlugin(groupId, artifactId)

        if ( plugin == null ) {
            violations.add(new RuleViolation(this, app.pomFile.path, 0, MISSING_PLUGIN + "$groupId , $artifactId"))
        } else {
            attributes.each {
                PomElement attribute = plugin.getAttribute(it.key)
                if ( attribute.value != it.value) {
                    violations.add(new RuleViolation(this, app.pomFile.path, attribute.lineNo,
                            RULE_VIOLATION_MESSAGE + "$it.key : $it.value"))
                }
            }
        }
        return violations
    }

}

