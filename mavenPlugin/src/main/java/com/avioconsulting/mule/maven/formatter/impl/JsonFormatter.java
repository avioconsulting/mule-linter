package com.avioconsulting.mule.maven.formatter.impl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonFormatter extends AbstractFormatter {

    @Override
    public void buildReport() throws IOException {
        String reportPath = this.mojo.getReportOutputDirectory().getAbsolutePath() + File.separator + "mule-linter-report.json";
        com.avioconsulting.mule.linter.model.rule.RuleExecutor re = this.ruleExecutor;
        FileOutputStream out = new FileOutputStream(reportPath);
        re.displayResults("json", out );
        this.mojo.getLog().info("Mule linter report saved in "+ reportPath);
    }

}
