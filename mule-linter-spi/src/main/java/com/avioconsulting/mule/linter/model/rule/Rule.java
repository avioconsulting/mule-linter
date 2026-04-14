package com.avioconsulting.mule.linter.model.rule;

import com.avioconsulting.mule.linter.model.Application;
import java.util.List;

/**
 * Base class for all linter rules.
 * Rule implementations must extend this class and implement the execute method.
 */
public abstract class Rule {
    
    private String ruleId;
    private String ruleName;
    private RuleSeverity severity = RuleSeverity.MINOR;
    private RuleType ruleType = RuleType.CODE_SMELL;
    
    /**
     * Default constructor for rules that will have properties set via DSL.
     */
    protected Rule() {
    }
    
    /**
     * Create a rule with all properties.
     */
    protected Rule(String ruleId, String ruleName, RuleSeverity severity, RuleType ruleType) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.severity = severity;
        this.ruleType = ruleType;
    }
    
    /**
     * Create a rule with rule ID and name, using default severity (CRITICAL) and type (CODE_SMELL).
     */
    protected Rule(String ruleId, String ruleName) {
        this(ruleId, ruleName, RuleSeverity.CRITICAL, RuleType.CODE_SMELL);
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public RuleSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }
    
    public RuleType getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
    
    /**
     * Initialize the rule after DSL configuration.
     * Override this method if the rule needs to perform setup after properties are set.
     */
    public void init() {
        // Default implementation does nothing
    }
    
    /**
     * Execute the rule against the application.
     * 
     * @param application the Mule application to analyze
     * @return list of violations found, or empty list if none
     */
    public abstract List<RuleViolation> execute(Application application);
}
