package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class CronExpressionConfigurableRule extends Rule {
    static final String RULE_ID = 'CRON_EXPRESSION_CONFIGURABLE'
    static final String RULE_NAME = 'Cron Expression is Configurable in a property file '
    static final String CRON_EXPRESSION_HARD_CODED = 'CRON Expression should be configured in a property file, currently it is hardcoded in the file :  '

    CronExpressionConfigurableRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.MAJOR

    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        //Auto discovery configuration would be check only in Global Config file

// Iterate over each configuration file using Application Model
        app.configurationFiles.each {
                // Assign the file to a temporary variable configFile
            configFile ->
                // Get all configurations within non Global Mule Configuration file
                List<MuleComponent> flowLevelComponentList = configFile.findNonGlobalConfigs()
                // If any components exist in the Mule Configuration File
                if (flowLevelComponentList!=null && flowLevelComponentList.size() > 0) {
                    flowLevelComponentList.each {
                        firstLevelComponent ->
                            if (firstLevelComponent.getChildren()!=null && firstLevelComponent.getChildren().size() > 0) {
                                firstLevelComponent.getChildren().each {
                                    secondLevelComponent ->
                                        if (secondLevelComponent.getChildren()!=null && secondLevelComponent.getChildren().size() > 0) {
                                            secondLevelComponent.getChildren().each {
                                                thirdLevelComponent ->
                                                    if (thirdLevelComponent.getChildren()!=null && thirdLevelComponent.getChildren().size() > 0) {

                                                        thirdLevelComponent.getChildren().each {
                                                            fourthLevelComponent ->
                                                                if (fourthLevelComponent.componentName!=null && fourthLevelComponent.componentName.equalsIgnoreCase("cron")) {
                                                                     if(fourthLevelComponent.getAttributeValue('expression')!=null && !(fourthLevelComponent.getAttributeValue('expression').startsWith('Mule::p(')
                                                                            || fourthLevelComponent.getAttributeValue('expression').startsWith('p(')
                                                                            || fourthLevelComponent.getAttributeValue('expression').startsWith('${')
                                                                            || fourthLevelComponent.getAttributeValue('expression').startsWith('#[') )){

                                                                        violations.add(new RuleViolation(this, configFile.file.path, fourthLevelComponent.lineNumber, CRON_EXPRESSION_HARD_CODED + configFile.file.name ))

                                                                    }

                                                                }

                                                        }
                                                    }
                                            }

                                        }

                                }
                            }
                    }

                }

        }


        return violations
    }
}