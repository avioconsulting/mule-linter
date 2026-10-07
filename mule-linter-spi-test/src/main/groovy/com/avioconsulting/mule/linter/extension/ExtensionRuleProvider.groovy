package com.avioconsulting.mule.linter.extension

import com.avioconsulting.mule.linter.catalog.RuleDefinition
import com.avioconsulting.mule.linter.extension.rules.HttpListenerPathRule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.spi.RuleProvider

class ExtensionRuleProvider implements RuleProvider {
    Collection<RuleDefinition> getRuleDefinitions() {
        [new RuleDefinition('http-listener-api-path', ['HTTP_LISTENER_API_PATH'], 'HTTP_LISTENER_API_PATH',
            'Require HTTP listener paths to be /api/*.', RuleSeverity.MINOR, RuleType.CODE_SMELL,
            HttpListenerPathRule, [], { Map options -> new HttpListenerPathRule() })]
    }
}
