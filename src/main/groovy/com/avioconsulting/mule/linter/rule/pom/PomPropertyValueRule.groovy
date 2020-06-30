package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation

class PomPropertyValueRule extends Rule {

    static final String RULE_ID = 'MAVEN_PROPERTY'
    static final String RULE_NAME = 'maven property match'
    static final String RULE_VIOLATION_MESSAGE = ' maven property does not match or exists in <properties></properties>'

    private final String propertyName
    private final String propertyValue

    PomPropertyValueRule(String propertyName, String propertyValue) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.propertyName = propertyName
        this.propertyValue = propertyValue
    }

    PomPropertyValueRule(String ruleId, String ruleName, String propertyName, String propertyValue) {
        this.ruleId = ruleId
        this.ruleName = ruleName
        this.propertyName = propertyName
        this.propertyValue = propertyValue
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        String pomPropertyValue = app.pomFile.getProperty(propertyName)
        if (!pomPropertyValue.equalsIgnoreCase(propertyValue)) {
            violations.add(new RuleViolation(this, app.pomFile.getFile().absolutePath,
                    0, propertyName + RULE_VIOLATION_MESSAGE))
        }

        return violations
    }

}
