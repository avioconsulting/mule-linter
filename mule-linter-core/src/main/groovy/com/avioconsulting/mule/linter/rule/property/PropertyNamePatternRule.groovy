package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
/**
 * This rule checks that properties name in the properties file follow a given case format.
 * It is recommended that the Properties name in the properties file follows predefined acceptable format
 * such as java property naming conventions (db.username).
 */
class PropertyNamePatternRule extends Rule {
    static final String RULE_ID = 'PROPERTY_NAME_PATTERN'
    static final String RULE_NAME = 'Properties name is following naming conventions.'
    static final String RULE_VIOLATION_MESSAGE = 'Properties name is not following naming conventions: '

    enum PropertyNameFormat {
        CAMEL_CASE,
        PASCAL_CASE,
        JAVA_PROPERTIES_CASE
    }
    /**
     * regex pattern for supported for PropertyNamePatternRule
     * CAMEL_CASE: Camel Case naming conventions - userName, userPassword.
     * PASCAL_CASE: Pascal Case naming conventions - UserName, UserPassword.
     * JAVA_PROPERTIES_CASE: Java property naming conventions - db.username, db.test.host.
     */
    static final Map<PropertyNameFormat,String> regex = [(PropertyNameFormat.CAMEL_CASE):'[a-z]+((\\d)|([A-Z0-9][a-z0-9]+))*([A-Z])?',
                                                         (PropertyNameFormat.PASCAL_CASE):'^[A-Z][a-zA-Z0-9]+$',
                                                         (PropertyNameFormat.JAVA_PROPERTIES_CASE):'^([a-z][a-z0-9]*)(\\.[a-z0-9]+)*$']

    PropertyNameFormat propertyNameFormat

    /**
     * format: naming format for this rule. the default value is `JAVA_PROPERTIES_CASE`
     * Current options are `CAMEL_CASE`, `PASCAL_CASE` or `JAVA_PROPERTIES_CASE`.
     */
    @Param("format") String format = 'JAVA_PROPERTIES_CASE'

    PropertyNamePatternRule(){
        super(RULE_ID, RULE_NAME, RuleSeverity.MINOR, RuleType.CODE_SMELL)
    }

    @Override
    void init(){
        if(format != null)
            try {
                this.propertyNameFormat = PropertyNameFormat.valueOf(format)
            }catch(Exception e){
                throw new IllegalArgumentException("Invalid format value: '"+format+"'. Current options are: " + PropertyNameFormat.values() )
            }

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
        return value.matches(regex.get(propertyNameFormat))
    }
}
