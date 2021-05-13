package com.avioconsulting.mule.maven.formatter;

import com.avioconsulting.mule.maven.mojo.MuleLinterMojo;
import org.apache.maven.plugin.logging.Log;

public interface IFormatter {

    IFormatter withRuleExecutor(com.avioconsulting.mule.linter.model.rule.RuleExecutor re);
   
    void buildReport();

    IFormatter withMojo(MuleLinterMojo mojo);

}
