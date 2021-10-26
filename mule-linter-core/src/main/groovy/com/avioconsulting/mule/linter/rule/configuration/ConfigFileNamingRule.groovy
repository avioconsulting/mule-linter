package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ConfigFileNamingRule extends Rule {

    static final String RULE_ID = 'CONFIG_FILE_NAMING'
    static final String RULE_NAME = 'Config files are following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'A Config file is not following naming conventions'
    CaseNaming caseNaming = new CaseNaming()

    ConfigFileNamingRule(CaseNaming.CaseFormat format) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        caseNaming.setFormat(format)
    }

    ConfigFileNamingRule() {
        this(CaseNaming.CaseFormat.KEBAB_CASE)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.configurationFiles.each {
            configFile ->
            String fileName = configFile.name.substring(0,configFile.name.lastIndexOf("."))
            if (!caseNaming.isValidFormat(fileName)) {
                violations.add(new RuleViolation(this, configFile.path,
                        0, RULE_VIOLATION_MESSAGE))
            }
        }
        return violations
    }
}
