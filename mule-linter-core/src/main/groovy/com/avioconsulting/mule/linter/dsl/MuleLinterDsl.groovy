package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.RuleSet

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

    def propertyMissing(String name) {
        methodMissing(name, null)
    }

    def methodMissing(String name, args) {
        def ruleClass = RulesLoader.getRuleClassById(name)
        if (!ruleClass) {
            throw new IllegalArgumentException("Unknown rule identifier '${name}'. Available rules: ${RulesLoader.rulesMap.keySet().sort().join(', ')}")
        }
        if (args != null && (args.length > 1 || (args.length == 1 && !(args[0] instanceof Closure)))) {
            throw new IllegalArgumentException("Rule '${name}' expects no arguments or one configuration closure")
        }
        def ruleObj = ruleClass.newInstance()
        if(args != null && args.length > 0 ) {
            Closure cl = args[0]
            cl.resolveStrategy = DELEGATE_ONLY
            cl.delegate = new RuleOptions(ruleObj)
            cl.call()
        }
        try {
            ruleObj.init()
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for rule '${name}': ${e.message}", e)
        }
        ruleSet.addRule(ruleObj)
    }
}
