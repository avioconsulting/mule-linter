package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.catalog.RuleCatalog
import com.avioconsulting.mule.linter.catalog.RuleSpecification

import static groovy.lang.Closure.DELEGATE_ONLY


class MuleLinterDsl {

    RulesDsl rulesDsl

    void rules(final Closure closure) {
        RulesDsl dsl = new RulesDsl()
        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()

        rulesDsl = dsl
    }
}

class RulesDsl{
    RuleSet ruleSet = new RuleSet()
    List<RuleSpecification> specifications = []

    void rule(String identifier, Closure configuration = null) {
        def definition = RuleCatalog.instance.resolve(identifier)
        def options = new RuleOptions(definition)
        if (configuration != null) {
            configuration.resolveStrategy = DELEGATE_ONLY
            configuration.delegate = options
            configuration.call()
        }
        RuleSpecification specification = definition.specification(options.suppliedOptions())
        addSpecification(specification)
    }

    void addSpecification(RuleSpecification specification) {
        // Domain validation also completes before any application is loaded.
        def instance = specification.instantiate()
        specifications.add(specification)
        ruleSet.addRule(instance)
    }

    def propertyMissing(String name) {
        methodMissing(name, null)
    }

    def methodMissing(String name, args) {
        RuleCatalog.instance.resolve(name)
        if (args != null && (args.length > 1 || (args.length == 1 && !(args[0] instanceof Closure)))) {
            throw new IllegalArgumentException("Rule '${name}' expects no arguments or one configuration closure")
        }
        rule(name, args != null && args.length > 0 ? args[0] as Closure : null)
    }
}
