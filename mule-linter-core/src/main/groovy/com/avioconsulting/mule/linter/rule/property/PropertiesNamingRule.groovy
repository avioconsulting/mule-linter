package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
/**
 * This rule checks that properties name in the properties file follow a given case format.
 * It is recommended that the Properties name in the properties file follows predefined acceptable format
 * such as java property naming conventions (db.username).
 */
class PropertiesNamingRule extends Rule {
    static final String RULE_ID = 'PROPERTIES_NAMING'
    static final String RULE_NAME = 'Properties name is following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'Properties name is not following naming conventions: '

    CaseNaming caseNaming

    /**
     * format: naming format for this rule. the default value is `JAVA_PROPERTIES_CASE`
     * It is recommended to pass `JAVA_PROPERTIES_CASE` for format.
     */
    @Param("format") String format = CaseNaming.CaseFormat.JAVA_PROPERTIES_CASE

    PropertiesNamingRule(){
        super(RULE_ID, RULE_NAME, RuleSeverity.MINOR, RuleType.CODE_SMELL)
        caseNaming = new CaseNaming(CaseNaming.CaseFormat.JAVA_PROPERTIES_CASE)
    }

    @Override
    void init(){
        if(format != null)
            try {
                caseNaming.setFormat((CaseNaming.CaseFormat.valueOf(format)))
            }catch(Exception e){
                throw new IllegalArgumentException("Invalid format value: '"+format+"'. Current options are: " + CaseNaming.CaseFormat.values() )
            }

    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []

        List<PropertyFile> propFiles = application.propertyFiles
        propFiles.each { file ->
            file.getProperties().each {
                String propName = it.key.toLowerCase()
                if (!caseNaming.isValidFormat(propName) ) {
                    violations.add(new RuleViolation(this, file.getFile().absolutePath,
                            0, RULE_VIOLATION_MESSAGE + propName))
                }
            }
        }
        return violations
    }
}
