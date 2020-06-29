package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation

class PomCheckMulePluginRule extends PomPropertyRule {

    static final String RULE_ID = "MULE_MAVEN_PLUGIN"
    static final String POM_FILE = "pom.xml"
    static final PROPERTY_NAME= "mule.maven.plugin.version"
    static final String RULE_NAME = "mule.maven.plugin.version maven property match"
    static final String RULE_VIOLATION_MESSAGE = "mule.maven.plugin.version maven property does not match or exists"

    String version

    public PomCheckMulePluginRule(String version) {
        super(RULE_ID, RULE_NAME)
        this.version = version
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = new ArrayList<RuleViolation>()

        String propertyValue = findPropertyVersion(this.PROPERTY_NAME)
        if (!propertyValue.equalsIgnoreCase(this.version)) {
            violations.add(new RuleViolation(this, POM_FILE, 0, RULE_VIOLATION_MESSAGE))
        }

        return violations
    }

}
