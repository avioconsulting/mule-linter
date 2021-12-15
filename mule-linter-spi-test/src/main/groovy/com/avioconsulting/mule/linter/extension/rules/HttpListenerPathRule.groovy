package com.avioconsulting.mule.linter.extension.rules


import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class HttpListenerPathRule extends Rule {

    static final String RULE_ID = 'HTTP_LISTENER_API_PATH'
    static final String RULE_NAME = 'HTTP Listener API Path'
    static final String RULE_VIOLATION_MESSAGE = 'HTTP Listener API Path must be "/api/*"'

    HttpListenerPathRule() {
        super(RULE_ID, RULE_NAME, RuleSeverity.MINOR, RuleType.CODE_SMELL)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        def httpComponents = application.findComponents(com.avioconsulting.mule.linter.extension.components.HttpComponent.IDENTIFIER_LISTENER.name, com.avioconsulting.mule.linter.extension.components.HttpComponent.IDENTIFIER_LISTENER.namespaceURI)
        List<RuleViolation> violations = []
        httpComponents.each { if(it.path != "/api/*") {
            violations.add(new RuleViolation(this, it.file.path, it.lineNumber, RULE_VIOLATION_MESSAGE))
        } }
        return violations
    }
}
