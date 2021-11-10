package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
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
    def ruleObj = new HashMap()

    def propertyMissing(String name) {
        methodMissing(name, null)
    }

    def methodMissing(String name, args) {
        def params = new LinkedHashMap()
        ruleObj.put(name,params)
        if(args != null && args.length > 0 ) {
            Closure cl = args[0]
            cl.resolveStrategy = DELEGATE_ONLY
            cl.delegate = params
            cl.call()
        }
    }
}

