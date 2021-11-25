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

class RuleObject{
    final String ruleId
    final Map params
    RuleObject(String ruleId, Map params){
        this.ruleId = ruleId
        this.params = params
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
            ruleSet.addRule(ruleObj)
            if(args != null && args.length > 0 ) {
                Closure cl = args[0]
                cl.resolveStrategy = DELEGATE_ONLY
                cl.delegate = ruleObj
                cl.call()
            }
        }
    }
}

