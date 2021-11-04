package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.rule.cicd.*
import com.avioconsulting.mule.linter.rule.configuration.*
import com.avioconsulting.mule.linter.rule.pom.*
import com.avioconsulting.mule.linter.rule.git.GitIgnoreRule

import static groovy.lang.Closure.DELEGATE_ONLY


class MuleLinterDsl {

    RulesDsl rulesDsl

    enum RULE {
        JENKINS_EXISTS(JenkinsFileExistsRule.class),
        AZURE_PIPELINES_EXISTS(AzurePipelinesExistsRule.class),
        GIT_IGNORE(GitIgnoreRule.class),
        CONFIG_FILE_NAMING(ConfigFileNamingRule.class),
        FLOW_SUBFLOW_NAMING(FlowSubflowNamingRule.class),
        MUNIT_VERSION(MunitVersionRule.class)

        final Class class_;
        private RULE(Class class_){this.class_ = class_}

    }

    void rules(final Closure closure) {
        RulesDsl dsl = new RulesDsl()
        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()

        rulesDsl = dsl
    }



    RuleSet getRules(Map rulesMap){

        RuleSet ruleSet = new RuleSet()

        rulesMap.each {
            //this can be replaced by a method for discover the rule
            Class ruleClass = RULE.valueOf(it.key).class_
            def params = it.value

            if(params instanceof Map && !((Map) params).isEmpty()){
                List paramList
                ruleClass.constructors.each {constructor ->{
                    try {
                        //paramList for instantiation constructor of rule by reflection
                        paramList = new ArrayList()
                        for(int i=0;i<constructor.parameterCount;i++){
                            Class paramClass = constructor.parameterTypes[i]
                            String paramValue = (params as Map).values().asList().get(i)
                            if (paramClass.isEnum()) {
                                paramList.add(Enum.valueOf(paramClass, paramValue.toString()))
                            } else {
                                paramList.add(paramValue)
                            }
                        }

                        if((params as Map).size() == paramList.size()){
                            return
                        }
                    }catch(Exception e){
                        e.printStackTrace()
                        //do nothing try next constructor
                    }
                }}
                if(paramList != null && (params as Map).size() == paramList.size()){
                    ruleSet.addRule(ruleClass.newInstance(paramList.toArray()))
                }
            }else{
                ruleSet.addRule(ruleClass.newInstance())
            }
        }
        return ruleSet
    }
}

class RulesDsl{
    def ruleObj = new HashMap()

    def propertyMissing(String name) {
        methodMissing(name, null)
    }

    def methodMissing(String name, args) {
        def params = new HashMap()
        ruleObj.put(name,params)
        if(args != null && args.length > 0 ) {
            Closure cl = args[0]
            cl.resolveStrategy = DELEGATE_ONLY
            cl.delegate = params
            cl.call()
        }
    }
}

