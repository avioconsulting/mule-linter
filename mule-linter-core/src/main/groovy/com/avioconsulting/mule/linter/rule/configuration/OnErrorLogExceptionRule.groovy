package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * Require exception logging at flow/shared boundaries; inline Try handlers are optional by default.
 */
class OnErrorLogExceptionRule extends Rule {

    static final String RULE_ID = 'ON_ERROR_LOG_EXCEPTION'
    static final String RULE_NAME = 'Exception should be logged after an error. '
    static final String RULE_VIOLATION_MESSAGE = 'Log exception enabled is required for ' +
                                                    'on-error-continue and on-error-propagate'
    private static final String ATTRIBUTE_NAME = 'logException'
    private static final String ATTRIBUTE_VALUE_CHECK = 'true'

    @Param('includeTryScopes') boolean includeTryScopes = false
    @Param('exceptions') List<Map> exceptions = []

    OnErrorLogExceptionRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    void init() {
        validateExceptions()
    }

    private void validateExceptions() {
        exceptions.each { exception ->
            if (!exception.file || !exception.handler || !exception.reason?.toString()?.trim() ||
                    !(exception.errorTypes instanceof List) || exception.errorTypes.empty ||
                    exception.errorTypes.any { !(it instanceof String) || !(it ==~ /[A-Za-z0-9_-]+:[A-Za-z0-9_-]+/) } ||
                    exception.file.toString().startsWith('/') || exception.file.toString().tokenize('/').contains('..')) {
                throw new IllegalArgumentException('Exception logging exemptions require an exact relative file, named handler, explicit namespace:error types (not ANY), and a reason')
            }
        }
    }

    @Override
    List<RuleViolation> execute(Application application) {
        // Retain validation for callers constructing rules directly outside the catalog.
        validateExceptions()
        List<RuleViolation> violations = []
        Set<Integer> used = [] as Set
        application.configurationFiles.each { file ->
            file.findErrorBranches(includeTryScopes).each { entry ->
                def comp = entry.component
                def types = (comp.getAttributeValue('type') ?: 'ANY').split(',').collect { it.trim() }
                boolean allowed = false
                exceptions.eachWithIndex { exception, index ->
                    if (GlobalConfigSeparationRule.relativePath(application, file.file) == exception.file &&
                            entry.handler == exception.handler && comp.getAttributeValue(ATTRIBUTE_NAME) == 'false' &&
                            types.every { exception.errorTypes.contains(it) }) {
                        allowed = true
                        used.add(index)
                    }
                }
                if (comp.getAttributeValue(ATTRIBUTE_NAME) != ATTRIBUTE_VALUE_CHECK && !allowed) {
                    violations.add(new RuleViolation(this, file.name, comp.lineNumber, RULE_VIOLATION_MESSAGE))
                }
            }
        }
        exceptions.eachWithIndex { exception, index ->
            if (!used.contains(index)) violations.add(new RuleViolation(this, exception.file.toString(), 0,
                'Unused exception logging exemption for handler: ' + exception.handler))
        }
        return violations
    }

}
