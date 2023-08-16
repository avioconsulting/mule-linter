package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.AVIOLoggerComponent
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for multiple Logger components of the same level being used back to back.
 * In general, multiple logging statements back to back at the same level are redundant and make code more difficult to read.
 * If you disagree with our standards, you can provide a custom limit broken down by logging level.
 */
class ConsecutiveLoggersCountRule extends Rule {
    static final String RULE_ID = 'CONSECUTIVE_LOGGERS_COUNT'
    static final String RULE_NAME = 'Loggers are not used excessively in sequence. '
    static final String RULE_VIOLATION_MESSAGE = 'Too many sequential loggers of same level in flow '

    private final EnumMap<LoggerComponent.LogLevel, Integer> privateExcessiveLoggers = [(LoggerComponent.LogLevel.TRACE): 2,
                                                                          (LoggerComponent.LogLevel.DEBUG): 2,
                                                                          (LoggerComponent.LogLevel.INFO) : 2,
                                                                          (LoggerComponent.LogLevel.WARN) : 2,
                                                                          (LoggerComponent.LogLevel.ERROR): 2]
    /**
     * excessiveLoggers: is an optional param representing the number of sequential loggers of the same level required to fail the rule.
     * The value can be an integer or an EnumMap, where the Enum is `LogLevel` found within the class `com.avioconsulting.mule.linter.model.configuration.LoggerComponent`.
     * The default is the integer value `2`, or the equivalent EnumMap:
     * excessiveLoggers = [
     *  TRACE: 2,
     *  DEBUG: 2,
     *  INFO : 2,
     *  WARN : 2,
     *  ERROR: 2
     * ]
     */
    @Param("excessiveLoggers") def excessiveLoggers

    ConsecutiveLoggersCountRule() {
        super(RULE_ID, RULE_NAME)
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
            if (it.componentName == LoggerComponent.COMPONENT_NAME || it.componentName == AVIOLoggerComponent.COMPONENT_NAME) {
                String componentLevelAttribute = it.getAttributeValue("level")
                // In AVIO Custom logger, since log level is not populated as attribute for default log level - 'INFO'
                // When log level attribute is not available in the configuration, default it to 'INFO'.
                if(componentLevelAttribute == null){
                    componentLevelAttribute = 'INFO'
                }
                if (componentLevelAttribute == logLevel) {
                    count++
                    if (count >= privateExcessiveLoggers.get(LoggerComponent.LogLevel.valueOf(
                            componentLevelAttribute))) {
                        violation = new RuleViolation(this, flow.file.path, flow.lineNumber, RULE_VIOLATION_MESSAGE
                                + flow.getAttributeValue("name"))
                    }
                } else {
                    logLevel = componentLevelAttribute
                    count = 1
                }
            }
        }
        return violation
    }
}
