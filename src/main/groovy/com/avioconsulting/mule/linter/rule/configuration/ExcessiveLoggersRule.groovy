package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ExcessiveLoggersRule extends Rule {
    static final String RULE_ID = 'EXCESSIVE_LOGGERS_RULE'
    static final String RULE_NAME = 'Excessive use of Sequential Loggers'
    static final String RULE_VIOLATION_MESSAGE = 'Too many sequential loggers of same level in flow '

    Map<String, Integer> excessiveLoggers = ["TRACE": 2, "DEBUG": 2, "INFO": 2, "WARN": 2, "ERROR": 2]

    ExcessiveLoggersRule() {}

    ExcessiveLoggersRule(Integer excessiveLoggers) {
        this(["TRACE": excessiveLoggers, "DEBUG": excessiveLoggers, "INFO": excessiveLoggers,
               "WARN": excessiveLoggers, "ERROR": excessiveLoggers])
    }

    ExcessiveLoggersRule(Map<String, Integer> excessiveLoggers) {
        this.ruleId = RULE_ID
        this.ruleName = RULE_NAME
        this.excessiveLoggers.putAll excessiveLoggers
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.flows.each { FlowComponent flow ->
            RuleViolation violation = searchFlowForLoggers(flow)
            if (violation != null) {
                violations.add(violation)
            }
        }
        application.subFlows.each { FlowComponent flow ->
            RuleViolation violation = searchFlowForLoggers(flow)
            if (violation != null) {
                violations.add(violation)
            }
        }
        return violations
    }

    private RuleViolation searchFlowForLoggers(FlowComponent flow) {
        String logLevel = null
        Integer count = 0
        RuleViolation violation = null
        flow.children.each {
            if (it.componentName == "logger") {
                if (it.getAttributeValue("level") == logLevel) {
                    count++
                    if (count >= excessiveLoggers.get(it.getAttributeValue("level"))) {
                        violation = new RuleViolation(this, flow.file.path, flow.lineNumber, RULE_VIOLATION_MESSAGE
                                + flow.getAttributeValue("name"))
                    }
                } else {
                    logLevel = it.getAttributeValue("level")
                    count = 1
                }
            }
        }
        return violation
    }
}
