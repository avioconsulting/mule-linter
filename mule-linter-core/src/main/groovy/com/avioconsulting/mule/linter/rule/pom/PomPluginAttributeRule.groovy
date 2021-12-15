package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.pom.PomPlugin
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for the existence of a given set of attributes in a given maven plugin within the `pom.xml`.
 * It exists as a customizable tool for a company to enforce a standard in their maven plugins.
 */
class PomPluginAttributeRule extends Rule {

    static final String RULE_ID = 'POM_PLUGIN_ATTRIBUTE'
    static final String RULE_NAME = 'The given Maven plugin exists in the pom.xml and matches the given attributes. '
    static final String MISSING_PLUGIN = 'Plugin does not exits: '
    static final String RULE_VIOLATION_MESSAGE = 'Plugin exist but does not matches the attribute: '

    /**
     * groupId: is a String representing the group id of the plugin to match against.
     * An example might be `"com.companyname"`.
     */
    @Param("groupId") String groupId

    /**
     * artifactId: is a String representing the artifact id of the plugin to match against.
     * An example might be `"example-plugin"`.
     */
    @Param("artifactId") String artifactId

    /**
     * attributes: is a Map representing the attributes that must be present in the plugin.
     * An example for the map might be:
     * ['examplekey':'examplevalue']
     */
    @Param("attributes") Map<String,String> attributes

    PomPluginAttributeRule(){
        super(RULE_ID, RULE_NAME)
    }
    PomPluginAttributeRule(String ruleId, String ruleName, String groupId, String artifactId){
        super(ruleId, ruleName)
        this.groupId = groupId
        this.artifactId = artifactId
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

