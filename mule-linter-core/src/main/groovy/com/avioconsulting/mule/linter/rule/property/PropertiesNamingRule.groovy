package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
/**
 * This rule checks that properties name in the properties file follow a given case format.
 * It is recommended that the Properties name in the properties file follows predefined acceptable format
 * such as java property naming conventions (db.username).
 */
class PropertiesNamingRule extends Rule {
    static final String RULE_ID = 'PROPERTIES_NAMING'
    static final String RULE_NAME = 'Properties name is following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'Properties name is not following naming conventions: '
    /**
     * regex pattern for java property naming conventions - db.username, db.test.host.
     */
    static final regex = '^([a-z][a-z0-9]*)(\\.[a-z0-9]+)*$'

    PropertiesNamingRule(){
        super(RULE_ID, RULE_NAME, RuleSeverity.MINOR, RuleType.CODE_SMELL)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []

        List<PropertyFile> propFiles = application.propertyFiles
        propFiles.each { file ->
            file.getProperties().each {
                String propName = it.key.toLowerCase()
                if (!isValidFormat(propName) ) {
                    violations.add(new RuleViolation(this, file.getFile().absolutePath,
                            0, RULE_VIOLATION_MESSAGE + propName))
                }
            }
        }
        return violations
    }

    Boolean isValidFormat(String value) {
        return value.matches(regex)
    }
}
