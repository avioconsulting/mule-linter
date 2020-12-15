package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import groovy.text.SimpleTemplateEngine

class PropertyExistsRule extends Rule {

    static final String RULE_ID = 'PROPERTY_EXISTS'
    static final String RULE_NAME = 'The Property File contains the specified property.'
    static final String RULE_VIOLATION_MESSAGE = 'Property missing: '
    static final String MISSING_FILE_MESSAGE = 'Property cannot be found, Property File is missing: '
    static final String DEFAULT_PATTERN = '${appname}-${env}.properties'
    static final List<String> DEFAULT_ENVIRONMENT_LIST = ['dev', 'test', 'prod']
    final String propertyName
    final List<String> environments
    final String pattern

    PropertyExistsRule(String propertyName) {
        this(propertyName, DEFAULT_ENVIRONMENT_LIST, DEFAULT_PATTERN)
    }

    PropertyExistsRule(String propertyName, List<String> environments) {
        this(propertyName, environments, DEFAULT_PATTERN)
    }

    PropertyExistsRule(String propertyName, List<String> environments, String pattern) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.propertyName = propertyName
        this.environments = environments
        this.pattern = pattern
    }

    @SuppressWarnings('UnnecessaryGetter')
    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        environments.each { env ->
            Map<String, String> binding = ['appname':application.name, 'env':env]
            String fileName = new SimpleTemplateEngine().createTemplate(pattern).make(binding)
            PropertyFile pf = application.propertyFiles.find { it.getName() == fileName }
            if (pf == null) {
                violations.add(new RuleViolation(this, fileName, 0, MISSING_FILE_MESSAGE + fileName))
            } else if (pf.getProperty(propertyName) == null) {
                violations.add(new RuleViolation(this, pf.getFile().path, 0, RULE_VIOLATION_MESSAGE + propertyName))
            }
        }
        return violations
    }

}
