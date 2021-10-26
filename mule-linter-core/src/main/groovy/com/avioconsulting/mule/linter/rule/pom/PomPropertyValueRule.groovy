package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class PomPropertyValueRule extends Rule {

    static final String RULE_ID = 'MAVEN_PROPERTY'
    static final String RULE_NAME = 'The given Maven property matches the given value. '
    static final String RULE_VIOLATION_MESSAGE = ' maven property value does not match expected value. '
    static final String ATTRIBUTE_MISSING_MESSAGE = ' does not exist in <properties></properties>'

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

        try {
            PomElement pomProperty = app.pomFile.getPomProperty(propertyName)
            if (!pomProperty.value.equalsIgnoreCase(propertyValue)) {
                violations.add(new RuleViolation(this, app.pomFile.path,
                        pomProperty.lineNo, pomProperty.name + RULE_VIOLATION_MESSAGE + 'Expected: ' + propertyValue
                        + ' found: ' + pomProperty.value))
            }
        } catch (IllegalArgumentException e) {
            violations.add(new RuleViolation(this, app.pomFile.path,
                    app.pomFile.propertiesLineNo, propertyName + ATTRIBUTE_MISSING_MESSAGE))
        }

        return violations
    }

}
