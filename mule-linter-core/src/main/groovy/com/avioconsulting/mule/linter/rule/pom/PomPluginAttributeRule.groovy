package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomPlugin
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class PomPluginAttributeRule extends Rule {

    static final String RULE_ID = 'POM_PLUGIN_ATTRIBUTE'
    static final String RULE_NAME = 'The given Maven plugin exists in the pom.xml and matches the given attributes. '
    static final String MISSING_PLUGIN = 'Plugin does not exits: '
    static final String RULE_VIOLATION_MESSAGE = 'Plugin exist but does not matches the attribute: '

    @Param("groupId") String groupId
    @Param("artifactId") String artifactId
    @Param("attributes") Map<String,String> attributes

    PomPluginAttributeRule(){
        super(RULE_ID, RULE_NAME)
    }
    PomPluginAttributeRule(String ruleId, String ruleName, String groupId, String artifactId){
        super(ruleId, ruleName)
        this.groupId = groupId
        this.artifactId = artifactId
    }

    PomPluginAttributeRule(String groupId, String artifactId, Map<String,String> attributes) {
        this(RULE_ID, RULE_NAME, groupId, artifactId, attributes)
    }

    PomPluginAttributeRule(String ruleId, String ruleName, String groupId, String artifactId, Map<String,String> attributes) {
        this(ruleId, ruleName, groupId, artifactId)
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

