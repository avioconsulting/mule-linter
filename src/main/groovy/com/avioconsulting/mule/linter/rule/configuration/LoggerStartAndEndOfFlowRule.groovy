package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class LoggerStartAndEndOfFlowRule extends Rule {

    static final String RULE_ID = 'LOGGER_START_AND_END'
    static final String RULE_NAME = 'All sub-flows should start with a DEBUG AVIO Logger of START and END of flow'
    static final String RULE_VIOLATION_MESSAGE = 'All sub-flows must contain a START and END AVIO logger with the level of DEBUG and a trace point of either START or END: '
    static final String RULE_VIOLATION_MESSAGE_MISSING_CAT = 'START/END AVIO Logger does not have a category defined: '

    public LoggerStartAndEndOfFlowRule() {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.setSeverity(RuleSeverity.MINOR)
    }

    RuleViolation checkLoggerConfig(MuleComponent mc, boolean first) {
        if(mc.componentName != "custom-logger" || mc.attributes.get("level") != "DEBUG") {
            return new RuleViolation(this, mc.file.path,
                    mc.lineNumber, RULE_VIOLATION_MESSAGE + mc.componentName)
        } else if(mc.attributes.get("category") == null) {
            return new RuleViolation(this, mc.file.path,
                    mc.lineNumber, RULE_VIOLATION_MESSAGE_MISSING_CAT + mc.componentName)
        }

        if(first) {
            if(mc.attributes.get("tracePoint") != "START")
                return new RuleViolation(this, mc.file.path,
                        mc.lineNumber, RULE_VIOLATION_MESSAGE + mc.componentName)
        } else {
            if(mc.attributes.get("tracePoint") != "END")
                return new RuleViolation(this, mc.file.path,
                        mc.lineNumber, RULE_VIOLATION_MESSAGE + mc.componentName)
        }

        return null
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        app.getAllFlows().each {flow ->
            if(flow.componentName == "sub-flow") {
                if(flow.children.size() > 0) {
                    MuleComponent first = flow.children.get(0)
                    MuleComponent last = flow.children.get(flow.children.size()-1)

                    RuleViolation[] arr = [checkLoggerConfig(first, true), checkLoggerConfig(last, false)]
                    arr.each {
                        if(it != null)
                            violations.add(it)
                    }


                }
            }
        }

        return violations
    }
}
