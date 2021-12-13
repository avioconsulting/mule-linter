package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that a config file matches a given case format.
 * Files, properties, and certain name attributes should generally follow a self consistent case pattern for readability and predictability.
 * At AVIO, we generally expect Mule Configs to follow `kebab-case`.
 */
class ConfigFileNamingRule extends Rule {

    static final String RULE_ID = 'CONFIG_FILE_NAMING'
    static final String RULE_NAME = 'Config files are following naming conventions. '
    static final String RULE_VIOLATION_MESSAGE = 'A Config file is not following naming conventions'
    CaseNaming caseNaming

    /**
     * naming format for this rule. the default value is `KEBAB_CASE`
     * Current options are `CAMEL_CASE`, `PASCAL_CASE`, `SNAKE_CASE`, or `KEBAB_CASE`.
     */
    @Param("format") String format

    ConfigFileNamingRule() {
        this.caseNaming = new CaseNaming(CaseNaming.CaseFormat.KEBAB_CASE)
    }

    @Override
    void init(){
        if(format != null)
            try {
                caseNaming.setFormat(CaseNaming.CaseFormat.valueOf(format))
            }catch(Exception e){
                throw new IllegalArgumentException("Invalid format value: '"+format+"'. Current options are: " + CaseNaming.CaseFormat.values() )
            }
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
