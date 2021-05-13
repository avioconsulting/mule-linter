package com.avioconsulting.mule.maven.formatter;

import com.avioconsulting.mule.maven.formatter.impl.ConsoleFormatter;
import com.avioconsulting.mule.maven.formatter.impl.HtmlFormatter;
import com.avioconsulting.mule.maven.formatter.impl.JsonFormatter;
import com.avioconsulting.mule.maven.mojo.MuleLinterMojo;

import javax.management.loading.MLetMBean;
import java.util.Optional;

public class FormatterBuilder {
    public static IFormatter build(FormatOptionsEnum format, MuleLinterMojo mojo, com.avioconsulting.mule.linter.model.rule.RuleExecutor re) {
        IFormatter formatter;

        switch (format){
            case CONSOLE:
                formatter =  new ConsoleFormatter();
                break;
            case JSON:
                formatter = new JsonFormatter();
                break;
            case HTML:
                formatter = new HtmlFormatter();
                break;
            default:
                throw new IllegalArgumentException(String.format("Formatter not supported for %s", format.getValue()));
                        
        }

        return formatter.withRuleExecutor(re).withMojo(mojo);

    }

}
