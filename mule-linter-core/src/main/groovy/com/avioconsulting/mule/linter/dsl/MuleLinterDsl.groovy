package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.CaseNaming
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSet
import com.avioconsulting.mule.linter.rule.cicd.*
import com.avioconsulting.mule.linter.rule.configuration.*
import com.avioconsulting.mule.linter.rule.git.GitIgnoreRule


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


    enum RULE {
        JENKINS_EXISTS(JenkinsFileExistsRule.class),
        AZURE_PIPELINES_EXISTS(AzurePipelinesExistsRule.class),
        GIT_IGNORE(GitIgnoreRule.class),
        CONFIG_FILE_NAMING(ConfigFileNamingRule.class),
        FLOW_SUBFLOW_NAMING(FlowSubflowNamingRule.class)

        final Class class_;
        private RULE(Class class_){this.class_ = class_}

        Rule createRule(){
            return class_.newInstance()
        }
        Rule createRule(Map params){
            return class_.newInstance(params.values().toArray())
        }
    }
}

class RulesDsl{

    protected final RuleSet rules = new RuleSet()


    private def CaseFormat = CaseNaming.CaseFormat
    private def RULE = MuleLinterDsl.RULE

    void addRule(final MuleLinterDsl.RULE rule) {
        addRule(rule, null)
    }

    void addRule(final MuleLinterDsl.RULE rule,final Map<String,Object> params) {
        if(params == null)
            rules.addRule(rule.createRule())
        else
            rules.addRule(rule.createRule(params))
    }

}

/*
class MuleLinterDsl {

    RuleDsl rulesDsl

    void rules(final Closure closure) {
        RuleDsl dsl = new RuleDsl()
        closure.delegate = dsl
        closure.resolveStrategy = DELEGATE_ONLY
        closure.call()

        rulesDsl = dsl
    }
}

class RuleDsl{

    private def CaseFormat = CaseNaming.CaseFormat
    protected final RuleSet rules = new RuleSet()

    void JENKINS_EXISTS(){rules.addRule(new JenkinsFileExistsRule())}
    void GIT_IGNORE() {rules.addRule(new GitIgnoreRule())}
    void CONFIG_FILE_NAMING(Map<String, CaseNaming.CaseFormat> params){
        rules.addRule(new ConfigFileNamingRule(params.get("format")))
    }
    void FLOW_SUBFLOW_NAMING(Map<String, CaseNaming.CaseFormat> params){
        rules.addRule(new FlowSubflowNamingRule(params.get("format")))
    }
}
 */