package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.Rule
import groovy.text.SimpleTemplateEngine

@SuppressWarnings(['GStringExpressionWithinString'])
class PropertyFileNamingRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_NAMING'
    static final String RULE_NAME = 'The Property Files match the given naming scheme. '
    static final String RULE_VIOLATION_MESSAGE = 'Missing property file, files must match naming pattern: '
    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'

    String[] environments
    String pattern

/**
 * A new PropertyFileNamingRule for a list of environments.  This ensures that
 * there is at least one file that matches the pattern '${appname}-${env}.properties'
 * for each environment.
 *
 * @param environments List of environments to check for files
 */
    PropertyFileNamingRule(List<String> environments) {
        this(environments, DEFAULT_PATTERN)
    }

/**
 * A new PropertyFileNamingRule for a list of environments.  This ensures that
 * there is at least one file that matches the pattern for each environment.
 * Possible pattern variables ${env} and ${appname}.
 *
 * @param environments List of environments to check for files
 * @param pattern String pattern to search. ex. '${appname}-${env}.properties'
 */
    PropertyFileNamingRule(List<String> environments, String pattern) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.environments = environments
        this.pattern = pattern
    }

    @SuppressWarnings('UnnecessaryGetter')
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List propertyFilenames = app.propertyFiles*.getName()
        environments.each { env ->
            Map<String, String> binding = ['appname':app.name, 'env':env]
            String fileName = new SimpleTemplateEngine().createTemplate(pattern).make(binding)
            if (!(fileName in propertyFilenames)) {
                violations.add(new RuleViolation(this, fileName, 0, RULE_VIOLATION_MESSAGE + pattern))
            }
        }

        return violations
    }

}
