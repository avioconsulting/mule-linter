package com.avioconsulting.mule.linter.rule.configuration

class UntilSuccessfulRule extends ComponentCountRule {
    static final String RULE_VIOLATION_MESSAGE = ' should be avoided if possible'

    UntilSuccessfulRule() {
        super('UNTIL_SUCCESSFUL_RULE', 'Until Successful Rule', "until-successful",
                "http://www.mulesoft.org/schema/mule/core", 1)
    }
}
