package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Namespace

/**
 * This rule checks that the until-successful component has not been used.
 * AVIO suggests that this componnent should be avoided if possible since failures should be dealt with, not ignored.
 */
class UntilSuccessfulRule extends ComponentCountRule {
    static final String RULE_ID = 'UNTIL_SUCCESSFUL'
    static final String RULE_VIOLATION_MESSAGE = ' should be avoided if possible'

    UntilSuccessfulRule() {
        super('UNTIL_SUCCESSFUL', 'Until Successful Rule', "until-successful", Namespace.CORE, 0)
    }
}
