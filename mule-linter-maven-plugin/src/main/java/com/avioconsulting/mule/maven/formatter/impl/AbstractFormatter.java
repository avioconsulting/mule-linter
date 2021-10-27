package com.avioconsulting.mule.maven.formatter.impl;

import com.avioconsulting.mule.maven.formatter.IFormatter;
import com.avioconsulting.mule.maven.mojo.AbstractMuleLinterMojo;
import org.apache.maven.plugin.logging.Log;

/**
 * @author aivaldi
 * Create a default formatter with access to Mojo plugin and ruleExecuter
 */
public abstract class AbstractFormatter implements IFormatter {

    protected com.avioconsulting.mule.linter.model.rule.RuleExecutor ruleExecutor;
    protected AbstractMuleLinterMojo mojo;


    @Override
    public IFormatter withRuleExecutor(com.avioconsulting.mule.linter.model.rule.RuleExecutor re) {
        this.ruleExecutor = re;
        return this;
    }


    @Override
    public IFormatter withMojo(AbstractMuleLinterMojo mojo) {
        this.mojo = mojo;
        return this;
    }

    protected Log getLog(){
        return mojo.getLog();
    }



}
