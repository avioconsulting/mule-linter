package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation
/**
 * Cron Expressions are used in most implementation using Schedulers/Batch/Polling based integrations.
 * This rule checks for all the Mule event source components with cron scheduler reference 'expression` configuration
 * using property (${cron.expression}) externalized in property files rather than be hardcoded in line.
 */
class CronExpressionExternalizedRule extends Rule {
    static final String RULE_ID = 'CRON_EXPRESSION_EXTERNALIZED'
    static final String RULE_NAME = 'Cron Expression is configured in a property file'
    static final String CRON_EXPRESSION_HARD_CODED = 'Cron expression should be configured in a property file, currently it is hardcoded in the file : '

    CronExpressionExternalizedRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.severity = RuleSeverity.MAJOR
    }
    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.flows.each{ flow ->
            if(flow.children.size()>0){
                // Get Mule Event source processor in the flow component, this would be the first component in the flow.
                MuleComponent muleComp = flow.children[0]
                violations.addAll checkCronExpressionViolations(muleComp)
            }
        }
        return violations
    }
    /*
    * This method checks if the cron configuration exists in the underlying Mule component, and validates if cron expression is referenced using property ${cron.expression} externalized in property files.
    * returns List<RuleViolation>
     */
    List<RuleViolation> checkCronExpressionViolations(MuleComponent muleComp) {
        List<RuleViolation> cronExpressionViolations =[]
        if(muleComp.componentName.equalsIgnoreCase('cron') ){
            String cronExpression = muleComp.getAttributeValue("expression")
            if(!cronExpression.startsWith('${')){
                cronExpressionViolations.add(new RuleViolation(this, muleComp.file.path, muleComp.lineNumber,
                        CRON_EXPRESSION_HARD_CODED + muleComp.file.name+". The hard coded cron expression is : "+ muleComp.getAttributeValue('expression')))
            }
        }else if(muleComp.children.size() > 0){
            muleComp.children.each {childComp ->
                cronExpressionViolations.addAll checkCronExpressionViolations(childComp)
            }
        }
        return cronExpressionViolations
    }
}