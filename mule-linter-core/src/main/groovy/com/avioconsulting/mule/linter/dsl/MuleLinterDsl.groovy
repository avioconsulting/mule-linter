package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.rule.cicd.*
import com.avioconsulting.mule.linter.rule.configuration.*
import com.avioconsulting.mule.linter.rule.pom.*
import com.avioconsulting.mule.linter.rule.git.GitIgnoreRule
import com.avioconsulting.mule.linter.rule.property.PropertyExistsRule

import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method

import static groovy.lang.Closure.DELEGATE_ONLY


class MuleLinterDsl {

    RulesDsl rulesDsl

    enum RULE {
        JENKINS_EXISTS(JenkinsFileExistsRule.class),
        AZURE_PIPELINES_EXISTS(AzurePipelinesExistsRule.class),
        GIT_IGNORE(GitIgnoreRule.class),
        CONFIG_FILE_NAMING(ConfigFileNamingRule.class),
        FLOW_SUBFLOW_NAMING(FlowSubflowNamingRule.class),
        MUNIT_VERSION(MunitVersionRule.class),
        PROPERTY_EXISTS(PropertyExistsRule.class)

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
            Object rule = ruleClass.newInstance()

            if(params instanceof Map && !((Map) params).isEmpty()){

                params.forEach((key,value)->{
                    try {
                        try {
                            //attempt to use setter method for param
                            PropertyDescriptor pd = new PropertyDescriptor(String.valueOf(key), rule.getClass());
                            Method setter = pd.getWriteMethod();
                            setter.invoke(rule, value.toString());
                        } catch (Exception e) {
                            //attempt to set value to param directly
                            Field field = rule.getClass().getDeclaredField(String.valueOf(key));
                            field.setAccessible(true)
                            Class paramClass = field.getClass()
                            if (paramClass.isEnum()) {
                                field.set(rule, Enum.valueOf(paramClass, value.toString()))
                            } else {
                                field.set(rule, value)
                            }
                        }
                    }catch(Exception e){
                        throw new NoSuchFieldException("[${key}] dont have getter and setter methods")
                    }
                })
            }

            ruleSet.addRule(rule as Rule)
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

