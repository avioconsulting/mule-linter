package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.model.Rule

class PropertyFileNamingRule extends Rule {

    static final String RULE_ID = 'PROPERTY_FILE_NAMING'
    static final String RULE_NAME = 'Property File Naming Rule'

    String[] environments

    PropertyFileNamingRule(List<String> environments) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.environments = environments
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        List propertyFilenames = []
        app.propertyFiles.each {
            println("Found property file: $it.name")
            propertyFilenames.add(it.name)
        }

        return violations
    }

}
