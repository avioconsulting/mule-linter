package com.avioconsulting.mule.maven.formatter.impl;


import com.avioconsulting.mule.linter.model.ReportFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonFormatter extends AbstractFormatter {

    @Override
    public void buildReport() throws IOException {
        if(this.mojo.getOutputDirectory() == null) {
            throw new IOException("Output directory not specified");
        }
        if (!this.mojo.getOutputDirectory().isDirectory() && !this.mojo.getOutputDirectory().mkdirs()) {
            throw new IOException("Could not create report directory: " + this.mojo.getOutputDirectory());
        }
        String reportPath = this.mojo.getOutputDirectory().getAbsolutePath() + File.separator + "mule-linter-report.json";
        com.avioconsulting.mule.linter.model.rule.RuleExecutor re = this.ruleExecutor;
        try (FileOutputStream out = new FileOutputStream(reportPath)) {
            re.displayResults(ReportFormat.JSON, out);
        }
        this.mojo.getLog().info("Mule Linter report saved in "+ reportPath);
    }

}
