package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Namespace

class UntilSuccessfulRule extends ComponentCountRule {
    static final String RULE_VIOLATION_MESSAGE = ' should be avoided if possible'

    UntilSuccessfulRule() {
        super('UNTIL_SUCCESSFUL', 'Until Successful Rule', "until-successful", Namespace.CORE, 1)
    }
}
