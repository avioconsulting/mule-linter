package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class ExcessiveLoggersRule extends Rule {
    static final String RULE_ID = 'EXCESSIVE_LOGGERS'
    static final String RULE_NAME = 'Loggers are not used excessively in sequence. '
    static final String RULE_VIOLATION_MESSAGE = 'Too many sequential loggers of same level in flow '

    private final EnumMap<LoggerComponent.LogLevel, Integer> privateExcessiveLoggers = [(LoggerComponent.LogLevel.TRACE): 2,
                                                                          (LoggerComponent.LogLevel.DEBUG): 2,
                                                                          (LoggerComponent.LogLevel.INFO) : 2,
                                                                          (LoggerComponent.LogLevel.WARN) : 2,
                                                                          (LoggerComponent.LogLevel.ERROR): 2]


    @Param("excessiveLoggers") def excessiveLoggers

    ExcessiveLoggersRule() {
        super(RULE_ID, RULE_NAME)
    }

    ExcessiveLoggersRule(@Param("excessiveLoggers") Integer excessiveLoggers) {
        this()
        this.excessiveLoggers = [(LoggerComponent.LogLevel.TRACE): excessiveLoggers,
                                             (LoggerComponent.LogLevel.DEBUG): excessiveLoggers,
                                             (LoggerComponent.LogLevel.INFO) : excessiveLoggers,
                                             (LoggerComponent.LogLevel.WARN) : excessiveLoggers,
                                             (LoggerComponent.LogLevel.ERROR): excessiveLoggers]
        init()
    }

    ExcessiveLoggersRule(EnumMap<LoggerComponent.LogLevel, Integer>  excessiveLoggers) {
        this()
        this.excessiveLoggers = excessiveLoggers
        init()
    }

    @Override
    void init(){
        if(excessiveLoggers != null) {
            if (excessiveLoggers instanceof Map) {

                Map<LoggerComponent.LogLevel, Integer> param = new EnumMap<>(LoggerComponent.LogLevel.class)
                excessiveLoggers.forEach((key, value) -> {
                    param.put(LoggerComponent.LogLevel.valueOf(key as String),value as Integer)
                })

                this.privateExcessiveLoggers.putAll(param)
            } else if(excessiveLoggers instanceof Integer){
                this.privateExcessiveLoggers.putAll([(LoggerComponent.LogLevel.TRACE): excessiveLoggers,
                                                     (LoggerComponent.LogLevel.DEBUG): excessiveLoggers,
                                                     (LoggerComponent.LogLevel.INFO) : excessiveLoggers,
                                                     (LoggerComponent.LogLevel.WARN) : excessiveLoggers,
                                                     (LoggerComponent.LogLevel.ERROR): excessiveLoggers])
            }
        }
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
            if (it.componentName == LoggerComponent.COMPONENT_NAME) {
                if (it.getAttributeValue("level") == logLevel) {
                    count++
                    if (count >= privateExcessiveLoggers.get(LoggerComponent.LogLevel.valueOf(
                            it.getAttributeValue("level")))) {
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
