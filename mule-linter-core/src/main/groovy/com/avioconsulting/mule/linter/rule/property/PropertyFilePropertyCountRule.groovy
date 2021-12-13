package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import groovy.text.SimpleTemplateEngine

/**
 * This rule ensures that the propety files for each environment have a matching number of properties in them.
 * While not always the case, a different number of properties in one environment might mean that a property was left out of an environment by mistake.
 */
@SuppressWarnings(['GStringExpressionWithinString'])
class PropertyFilePropertyCountRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_COUNT_MISMATCH'
    static final String RULE_NAME = 'Property Files should exist for each environment. '
    static final String RULE_VIOLATION_MESSAGE = 'Properties files do not have matching number of properties per environment. '
    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'

    /**
     * environments: is a list of environments used to check for the number of properties per environment.
     * For example: ['dev', 'test', 'prod']
     */
    @Param("environments") List<String> environments

    /**
     * pattern: is the basis for how a property file for a given environment is named.
     * The default pattern is `"${appname}-${env}.properties"`.
     */
    @Param("pattern") String pattern

    PropertyFilePropertyCountRule() {
        super(RULE_ID, RULE_NAME)
        this.environments = []
        this.pattern = DEFAULT_PATTERN
    }

    @SuppressWarnings('UnnecessaryGetter')
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List<PropertyFile> propFiles = getValidPropertyFiles(getValidPropertyFilenames(app.name), app.propertyFiles)

        if (propFiles*.getPropertyCount().unique().size() > 1) {
            Map counts = propFiles.collectEntries { [it.getName(), it.getPropertyCount()] }.sort()
            propFiles.each { file ->
                violations.add(new RuleViolation(this, file.getName(), 0, RULE_VIOLATION_MESSAGE + counts))
            }
        }

        return violations
    }

    List getValidPropertyFilenames(String applicationName) {
        List validPropertyFilenames = []
        environments.each { env ->
            Map<String, String> binding = ['appname':applicationName, 'env':env]
            String fileName = new SimpleTemplateEngine().createTemplate(pattern).make(binding)
            validPropertyFilenames.add(fileName)
        }
        return validPropertyFilenames
    }

    @SuppressWarnings('UnnecessaryGetter')
    List getValidPropertyFiles(List validPropertyFilenames, List propertyFiles) {
        List<PropertyFile> validPropertyFiles = []
        propertyFiles.each {
            if (it.getName() in validPropertyFilenames) {
                validPropertyFiles.add(it as PropertyFile)
            }
        }
        return validPropertyFiles
    }

}
