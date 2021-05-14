package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
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

    boolean isExternalizedValue(String str) {
        return str.startsWith('#[') || str.startsWith('${') || str.startsWith('p(')
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.getAllFlows().each {flow ->
            flow.getChildren().each {
                if(it.componentNamespace.contains("/schema/mule/email")) {
                    it.attributes.each {entry->
                        if(entry.key.toLowerCase().contains("address")) {
                            if(!isExternalizedValue(entry.value)) {
                                violations.add(new RuleViolation(this, flow.file.path,
                                        it.lineNumber, RULE_VIOLATION_MESSAGE + it.componentName))
                            }
                        }
                    }
                    it.children.each {child->
                        if(child.componentName.toLowerCase().contains("address")) {
                            child.children.each {
                                if(!isExternalizedValue(it.attributes.get("value"))) {
                                    violations.add(new RuleViolation(this, flow.file.path,
                                            it.lineNumber, RULE_VIOLATION_MESSAGE + it.componentName))
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
