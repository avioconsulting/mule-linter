package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Namespace
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks that the all the Flow must have error handler (global, reference or unique).
 * It is recommended default global error handler is configured for the application. When there is no global error handler configured, all the flow must have reference or unique implementation of error handler.
 */
class FlowErrorHandlerRule extends Rule {
    static final String RULE_ID = 'FLOW_ERROR_HANDLER'
    static final String RULE_NAME = 'Flow must have error handler, when global error handler is not configured.'
    static final String RULE_VIOLATION_MESSAGE = ' flow must have error handler, when default global error handler is not configured.'

    final static String CONFIGURATION_NAMESPACE = Namespace.CORE
    final static String CONFIGURATION_NAME = 'configuration'
    final static String DEFAULT_ERROR_HANDLER_ATTRIBUTE = 'defaultErrorHandler-ref'
    final static String ERROR_HANDLER_COMPONENT_NAME = 'error-handler'

    FlowErrorHandlerRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        def components = application.findComponents(CONFIGURATION_NAME, CONFIGURATION_NAMESPACE)
        boolean isGlobalErrorHandler = false
        for (MuleComponent muleComponent: components){
            if(muleComponent.hasAttributeValue(DEFAULT_ERROR_HANDLER_ATTRIBUTE)){
                isGlobalErrorHandler = true
            }
        }
        // Execute when there is no Default Global Error Handler configuration
        if(!isGlobalErrorHandler) {
            def flowComponents = application.getFlows()
            for (FlowComponent flowComponent: flowComponents){
                boolean isFlowErrorHandler = false
                flowComponent.children.each {
                    if(it.componentName == ERROR_HANDLER_COMPONENT_NAME){
                        isFlowErrorHandler = true
                    }
                }
                // Raise error when there's no Global error handler and flow handler
                if(isFlowErrorHandler == false){
                    violations.add(new RuleViolation(this, flowComponent.file.path,
                            flowComponent.lineNumber , flowComponent.getAttributeValue("name") + RULE_VIOLATION_MESSAGE))
                }
            }
        }
        return violations
    }
}
