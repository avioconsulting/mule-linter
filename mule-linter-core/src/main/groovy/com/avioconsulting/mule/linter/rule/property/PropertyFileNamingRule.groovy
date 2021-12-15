package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.model.rule.Rule
import groovy.text.SimpleTemplateEngine

/**
 * This rule ensures that property files exist for each deployment environment, according to an optionally given pattern.
 * Using string interpolation, it is possible to load a specific property file depending on other parameters such as environment.
 * AVIO highly recommends that organizations take full advantage of that feature to avoid cross contamination between testing environments and production.
 */
@SuppressWarnings(['GStringExpressionWithinString'])
class PropertyFileNamingRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_NAMING'
    static final String RULE_NAME = 'The Property Files match the given naming scheme. '
    static final String RULE_VIOLATION_MESSAGE = 'Missing property file, files must match naming pattern: '
    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'

    /**
     * environments: is a list of environments used to check for the existence of property files for those environments.
     * For example: ['dev', 'test', 'prod']
     */
    @Param("environments") List<String> environments

    /**
     * pattern: is the basis for how a property file for a given environment is named.
     * The default pattern is `"${appname}-${env}.properties"`.
     */
    @Param("pattern") String pattern

    PropertyFileNamingRule() {
        super(RULE_ID, RULE_NAME)
        this.environments = []
        this.pattern = DEFAULT_PATTERN
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
