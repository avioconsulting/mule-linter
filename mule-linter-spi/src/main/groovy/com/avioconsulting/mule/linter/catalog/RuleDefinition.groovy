package com.avioconsulting.mule.linter.catalog

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType

/** Explicit metadata and a fresh-instance factory shared by all configuration clients. */
final class RuleDefinition {
    final String id
    final List<String> aliases
    final String reportId
    final String description
    final RuleSeverity defaultSeverity
    final RuleType defaultType
    final Class<? extends Rule> ruleClass
    final Map<String, OptionDefinition> options
    private final Closure<Rule> factory

    RuleDefinition(String id, List<String> aliases, String reportId, String description,
                   RuleSeverity severity, RuleType type, Class<? extends Rule> ruleClass,
                   List<OptionDefinition> options, Closure<Rule> factory) {
        if (!(id ==~ /[a-z][a-z0-9]*(?:-[a-z0-9]+)*/)) throw new IllegalArgumentException("Invalid canonical rule ID '$id'")
        if (!description?.trim() || !reportId || !severity || !type || !ruleClass || !factory) throw new IllegalArgumentException("Incomplete definition for '$id'")
        this.id = id
        this.aliases = Collections.unmodifiableList(new ArrayList<>(aliases))
        this.reportId = reportId
        this.description = description.trim()
        this.defaultSeverity = severity
        this.defaultType = type
        this.ruleClass = ruleClass
        this.factory = factory
        List<OptionDefinition> all = options + [
            OptionDefinition.enumeration('severity', RuleSeverity).description('Finding severity.').defaultValue(severity),
            OptionDefinition.enumeration('ruleType', RuleType).description('Finding classification.').defaultValue(type),
            OptionDefinition.string('ruleName').description('Override the human-readable report label.')]
        if (all*.name.toSet().size() != all.size()) throw new IllegalArgumentException("Duplicate options for '$id'")
        this.options = Collections.unmodifiableMap(all.collectEntries { [(it.name): it] })
    }

    RuleSpecification specification(Map supplied = [:]) { new RuleSpecification(this, supplied) }

    Rule instantiate(Map normalized) {
        try {
            Rule rule = factory.call(normalized.findAll { !(it.key in ['severity', 'ruleType', 'ruleName']) })
            if (rule == null || !ruleClass.isInstance(rule)) throw new IllegalStateException('Factory returned an invalid rule instance')
            rule.ruleId = reportId
            rule.severity = normalized.severity
            rule.ruleType = normalized.ruleType
            if (normalized.containsKey('ruleName')) rule.ruleName = normalized.ruleName
            rule.init()
            rule
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for rule '$reportId' ($id): ${e.message}", e)
        }
    }

    Map describe() {
        [id: id, aliases: aliases, reportId: reportId, description: description,
         defaultSeverity: defaultSeverity.name(), defaultType: defaultType.name(),
         options: options.collectEntries { name, option -> [(name): option.describe()] }]
    }
}
