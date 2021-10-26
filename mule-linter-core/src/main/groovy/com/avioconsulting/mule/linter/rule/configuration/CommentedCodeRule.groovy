package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ConfigurationFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

class CommentedCodeRule extends Rule {

    static final String RULE_ID = 'COMMENTED_CODE'
    static final String RULE_NAME = 'Commented out code should be removed. '
    static final String RULE_VIOLATION_MESSAGE = 'Code is commented out in a configuration file'

    Pattern COMMENT_REGEX = ~/<!--[\s\S\n]*?-->/
    Pattern CODE_REGEX = ~/<.[\s\S\n]*?(?:\/>|<\/)/

    CommentedCodeRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.each { ConfigurationFile configurationFile ->
            (configurationFile.file.text =~ COMMENT_REGEX).collect({String it -> it}).unique().each { String comment ->
                if (comment =~ CODE_REGEX) {
                    Integer lineNumber = configurationFile.file.text.substring(0,
                            configurationFile.file.text.indexOf(comment)).count("\n") + 1
                    violations.add(new RuleViolation(this, configurationFile.file.path, lineNumber,
                            RULE_VIOLATION_MESSAGE))
                }
            }
        }
        return violations
    }
}
