package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class CronExpressionConfigurableRule extends Rule {
    static final String RULE_ID = 'CRON_EXPRESSION_EXTERNALIZED'
    static final String RULE_NAME = 'Cron Expression is configured in a property file '
    static final String CRON_EXPRESSION_HARD_CODED = 'CRON Expression should be configured in a property file, currently it is hardcoded in the file :  '

    CronExpressionConfigurableRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.MAJOR

    }
    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        if(app.configurationFiles!=null && app.configurationFiles.size() >0){
        app.configurationFiles.each {
            configFile ->
                List<MuleComponent> flowLevelComponentList = configFile.findNonGlobalConfigs()
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
                                                                     if(!fourthLevelComponent.isExternalized(fourthLevelComponent.getAttributeValue('expression'))){
                                                                        violations.add(new RuleViolation(this, configFile.file.path, fourthLevelComponent.lineNumber, CRON_EXPRESSION_HARD_CODED + configFile.file.name+". The hard coded cron expression is : "+ fourthLevelComponent.getAttributeValue('expression') ))
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
        }
        return violations
    }
}