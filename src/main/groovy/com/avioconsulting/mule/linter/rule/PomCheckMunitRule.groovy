package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation

class PomCheckMunitRule extends PomPropertyRule {

    static final String RULE_ID = "MUNIT_VERSION"
    static final String POM_FILE = "pom.xml"
    static final PROPERTY_NAME= "munit.version"
    static final String RULE_NAME = "munit.version maven property match"
    static final String RULE_VIOLATION_MESSAGE = "munit.version maven property does not match or exists"

    String version

    public PomCheckMunitRule(String version) {
        super(RULE_ID, RULE_NAME)
        this.version = version
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = new ArrayList<RuleViolation>()

        String propertyValue = findPropertyVersion(app, this.PROPERTY_NAME)
        if (!propertyValue.equalsIgnoreCase(this.version)) {
            violations.add(new RuleViolation(this, POM_FILE, 0, RULE_VIOLATION_MESSAGE))
        }

        return violations
    }
}