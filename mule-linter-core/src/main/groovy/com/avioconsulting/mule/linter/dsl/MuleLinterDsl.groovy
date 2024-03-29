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
        if(ruleClass){
            def ruleObj = ruleClass.newInstance()
            if(args != null && args.length > 0 ) {
                Closure cl = args[0]
                cl.resolveStrategy = DELEGATE_ONLY
                cl.delegate = ruleObj
                cl.call()
            }
            ruleObj.init()
            ruleSet.addRule(ruleObj)
        }
    }
}

