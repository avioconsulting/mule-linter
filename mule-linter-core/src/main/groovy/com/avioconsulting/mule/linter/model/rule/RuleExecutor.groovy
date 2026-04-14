package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application

/**
 * Executes rules against an application and collects violations.
 * Formatting of results is handled by IReportFormatter implementations.
 */
class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    Integer ruleCount = 0

    RuleExecutor(Application application, List<RuleSet> rules) {
        this.rules = rules
        this.application = application
    }

    /**
     * Execute all rules against the application.
     * Results are stored in this.results.
     */
    void executeRules() {
        rules.each { ruleSet ->
            ruleSet.getRules().each { rule ->
                results.addAll(rule.execute(application))
                ruleCount++
            }
        }
    }

    /**
     * Check if any violations exist.
     */
    boolean hasErrors() {
        this.results.size() > 0
    }
}
