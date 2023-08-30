package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.pom.PomElement
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for the existence of a property with a given value in the `pom.xml`.
 * It exists as a customizable tool for a company to enforce a standard for maven versioning.
 */
class PomPropertyValueRule extends Rule {

    static final String RULE_ID = 'MAVEN_PROPERTY'
    static final String RULE_NAME = 'The given Maven property matches the given value. '
    static final String RULE_VIOLATION_MESSAGE = ' maven property value does not match expected value. '
    static final String ATTRIBUTE_MISSING_MESSAGE = ' does not exist in <properties></properties>'

    /**
     * propertyName: is the key to be set in the `pom.xml`.
     */
    @Param("propertyName") String propertyName

    /**
     * propertyValue: is the value to be set in the `pom.xml`.
     */
    @Param("propertyValue") String propertyValue

    PomPropertyValueRule(){
        super(RULE_ID, RULE_NAME)
    }

    PomPropertyValueRule(String ruleId, String ruleName, String propertyName){
        super(ruleId, ruleName)
        this.propertyName = propertyName
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        try {
            PomElement pomProperty = app.pomFile.getPomProperty(propertyName)
            if (!pomProperty.value.equalsIgnoreCase(propertyValue)) {
                violations.add(new RuleViolation(this, app.pomFile.path,
                        0, pomProperty.name + RULE_VIOLATION_MESSAGE + 'Expected: ' + propertyValue
                        + ' found: ' + pomProperty.value))
            }
        } catch (IllegalArgumentException e) {
            violations.add(new RuleViolation(this, app.pomFile.path,
                    0, propertyName + ATTRIBUTE_MISSING_MESSAGE))
        }

        return violations
    }

}
