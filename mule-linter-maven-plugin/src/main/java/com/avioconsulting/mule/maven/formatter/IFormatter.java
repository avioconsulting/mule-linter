package com.avioconsulting.mule.maven.formatter;

import com.avioconsulting.mule.maven.mojo.AbstractMuleLinterMojo;

import java.io.IOException;

public interface IFormatter {

    IFormatter withRuleExecutor(com.avioconsulting.mule.linter.model.rule.RuleExecutor re);
   
    void buildReport() throws IOException;

    IFormatter withMojo(AbstractMuleLinterMojo mojo);

}
