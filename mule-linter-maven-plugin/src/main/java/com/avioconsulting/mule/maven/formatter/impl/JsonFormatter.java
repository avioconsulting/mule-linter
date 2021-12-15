package com.avioconsulting.mule.maven.formatter.impl;


import com.avioconsulting.mule.linter.model.ReportFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonFormatter extends AbstractFormatter {

    @Override
    public void buildReport() throws IOException {
        if(this.mojo.getOutputDirectory() == null) {
            this.mojo.getLog().warn("Output directory not specified");
        }
        this.mojo.getOutputDirectory().mkdirs();
        String reportPath = this.mojo.getOutputDirectory().getAbsolutePath() + File.separator + "mule-linter-report.json";
        com.avioconsulting.mule.linter.model.rule.RuleExecutor re = this.ruleExecutor;
        FileOutputStream out = new FileOutputStream(reportPath);
        re.displayResults(ReportFormat.JSON, out );
        this.mojo.getLog().info("Mule Linter report saved in "+ reportPath);
    }

}
