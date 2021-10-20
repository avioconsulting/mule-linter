package com.avioconsulting.mule.maven.mojo;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.maven.formatter.FormatOptionsEnum;
import com.avioconsulting.mule.maven.formatter.FormatterBuilder;
import com.avioconsulting.mule.maven.formatter.impl.ConsoleFormatter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Mojo mule-linter goal
 * Allows to execute mule-linter given the project source directory and groovy rules
 */
@Mojo(name = "mule-linter", defaultPhase = LifecyclePhase.INITIALIZE)
public class MuleLinterMojo extends AbstractMavenReport {

    @Parameter(property = "ruleConfiguration", readonly = true, required = true)
    private String ruleConfiguration;

    @Parameter(property = "appDir", readonly = true, required = true)
    private String appDir;

    @Parameter(property = "formats", readonly = true, required = false)
    private List<String> formats;

    @Parameter(property = "failBuild", readonly = true, required = false, defaultValue = "false")
    private Boolean failBuild;

    public String getOutputName() {
        // This report will generate simple-report.html when invoked in a project with `mvn site`
        return "mule-linter-results";
    }

    public String getName(Locale locale) {
        // Name of the report when listed in the project-reports.html page of a project
        return "Mule linter";
    }

    public String getProjectName (){
        return this.project.getName();
    }

    public String getProjectVersion (){
        return this.project.getVersion();
    }


    public String getDescription(Locale locale) {
        // Description of the report when listed in the project-reports.html page of a project
        return "This simple report is a very simple report that does nothing but "
                + "shows off Maven's wonderful reporting capabilities.";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        getLog().debug("mule-linter start");
        getLog().debug(String.format("RuleConfiguration file: %s appDirectory %s", ruleConfiguration, appDir));
        getLog().debug(String.format("Formats: %s", this.formats));
        try {
            MuleLinter ml = new MuleLinter(appDir, ruleConfiguration, null);
            com.avioconsulting.mule.linter.model.rule.RuleExecutor re = ml.buildLinterExecutor();
            generateReport(re);
            if(failBuild && re.hasErrors())
                throw new MavenReportException("Errors detected in mule validation");

        }catch (Exception e) {
            getLog().error(e);
            throw new MavenReportException ("Validation failed, see log messages", e);
        }

        getLog().debug("mule-linter end");
    }

    private void generateReport(com.avioconsulting.mule.linter.model.rule.RuleExecutor re) throws IOException, MavenReportException {
        for( String format: formats ){
            if (this.getReportOutputDirectory().exists() && !this.getReportOutputDirectory().isDirectory())
                throw new MavenReportException("Cannot create directory");
            else if (!this.getReportOutputDirectory().exists())
                this.getReportOutputDirectory().mkdirs();
            Optional<FormatOptionsEnum> formatOptionsOptional = FormatOptionsEnum.fromString(format);
            if (formatOptionsOptional.isPresent())
            {
                this.getLog().debug(String.format("Formatter found for %s", format));
                FormatterBuilder.build(formatOptionsOptional.get(), this, re).buildReport();

            }
            else{
                this.getLog().warn(String.format("No formatter found for %s", format));
            }
        }
    }
}