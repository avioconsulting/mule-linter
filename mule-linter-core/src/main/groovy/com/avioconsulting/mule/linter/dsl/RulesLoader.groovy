package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSet
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class RulesLoader {

    private static final def Map<String, Class<? extends Rule>> rulesMap = [:]
    static {
        //This loads all Rule classes shipped with core.
        //TODO: Find a way to load all classes from external library that can have different package.
        Reflections rf = new Reflections(new ConfigurationBuilder()
                .forPackages("com.", "org.", "io.")
                .setExpandSuperTypes(false)
                .addScanners(Scanners.values()))
        def rules = rf.getSubTypesOf(Rule.class)
        //Look for field named RULE_ID
        rules.each { rule -> {
            rulesMap.put(rule.RULE_ID, rule)
        }}
    }
    static def Map<String, Class<? extends Rule>> getRulesMap() {
        return rulesMap
    }
    static def Class<Rule> getRuleClassById(String ruleId) {
        return rulesMap.get(ruleId)
    }


    static RuleSet getRules(List<RuleObject> rulesMList){

        RuleSet ruleSet = new RuleSet()

        rulesMList.each {
            //this can be replaced by a method for discover the rule
            String ruleId = it.ruleId as String
            Map<String, Object> params = it.params as Map<String, Object>

            Class ruleClass = RulesLoader.getRuleClassById(ruleId)
            if(ruleClass != null){
                try {
                    ruleSet.addRule(Rule.createRule(ruleClass,params))
                }catch(Exception e){
                    println("error creating rule <<${ruleId}>>")
                }
            } else {
                println("rule <<${ruleId}>> not found!")
            }

        }
        return ruleSet
    }
}