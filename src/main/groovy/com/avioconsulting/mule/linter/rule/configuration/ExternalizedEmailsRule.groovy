package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.EmailComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ExternalizedEmailsRule extends Rule {

    static final String RULE_ID = 'EXTERNALIZED_EMAILS'
    static final String RULE_NAME = 'All emails are . '
    static final String RULE_VIOLATION_MESSAGE = 'The email address value is not store externally in a properties file: '

    ExternalizedEmailsRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.setSeverity(RuleSeverity.MINOR)
    }



    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.getAllFlows().each {flow ->
            flow.getChildren().each {
                EmailComponent ec = new EmailComponent(it, it.file)
                ec.getAllEmails().each { email ->
                    if(!ec.isExternalizedValue(email)) {
                        violations.add(new RuleViolation(this, flow.file.path,
                                ec.lineNumber, RULE_VIOLATION_MESSAGE + ec.componentName))
                    }
                }
            }
        }
        return violations
    }
}
