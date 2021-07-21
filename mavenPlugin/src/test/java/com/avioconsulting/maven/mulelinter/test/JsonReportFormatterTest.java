package org.avioconsulting.maven.mulelinter.test;

import com.avioconsulting.mule.linter.model.rule.RuleExecutor;
import com.avioconsulting.mule.linter.model.rule.RuleSet;
import com.avioconsulting.mule.maven.formatter.FormatOptionsEnum;
import com.avioconsulting.mule.maven.formatter.FormatterBuilder;
import com.avioconsulting.mule.maven.formatter.IFormatter;
import com.avioconsulting.mule.maven.formatter.impl.ConsoleFormatter;
import com.avioconsulting.mule.maven.formatter.impl.JsonFormatter;
import com.avioconsulting.mule.maven.mojo.MuleLinterMojo;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
@Ignore
public class JsonReportFormatterTest {
    
    
    @Test
    public void getCorrectFormatter(){

        IFormatter consoleFormatter = FormatterBuilder.build(FormatOptionsEnum.fromString("console").get(),null, null);
        assertTrue(consoleFormatter instanceof ConsoleFormatter);

        consoleFormatter = FormatterBuilder.build(FormatOptionsEnum.fromString("json").get(),null, null);
        assertTrue(consoleFormatter instanceof JsonFormatter);

    }
    
    @Test
    public void testFormatterNoViolation(){

        MuleLinterMojo mojo = new MuleLinterMojo();


        List<com.avioconsulting.mule.linter.model.rule.RuleViolation> violations = new ArrayList<>();
        List<com.avioconsulting.mule.linter.model.rule.RuleSet> rules = new ArrayList<>();
        com.avioconsulting.mule.linter.model.rule.RuleSet ruleSet = new RuleSet();
        ruleSet.addRule(new com.avioconsulting.mule.linter.rule.cicd.JenkinsFileExistsRule());
        rules.add(ruleSet);
        com.avioconsulting.mule.linter.model.rule.RuleExecutor re = new RuleExecutor(null, rules);

        IFormatter consoleFormatter = FormatterBuilder.build(FormatOptionsEnum.fromString("console").get(), mojo, re);

        assertNotNull(consoleFormatter);
        


    }
}
