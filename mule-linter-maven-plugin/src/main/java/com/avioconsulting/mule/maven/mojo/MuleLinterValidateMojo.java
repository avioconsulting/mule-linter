package com.avioconsulting.mule.maven.mojo;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.model.ReportFormat;
import com.avioconsulting.mule.linter.model.rule.RuleExecutor;
import com.avioconsulting.mule.maven.formatter.FormatOptionsEnum;
import com.avioconsulting.mule.maven.formatter.FormatterBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

@Mojo(name = "validate",
        defaultPhase = LifecyclePhase.VALIDATE)
public class MuleLinterValidateMojo extends AbstractMuleLinterMojo {

    @Parameter(property = "ruleConfiguration", defaultValue = "${basedir}/muleLinter.groovy" ,readonly = true, required = false)
    private File ruleConfiguration;

    @Parameter(property = "appDir", defaultValue = "${basedir}" ,readonly = true, required = false)
    private File appDir;

    @Parameter(property = "format", defaultValue = "CONSOLE,JSON", readonly = true, required = false)
    private List<FormatOptionsEnum> formats;

    @Parameter(property = "failBuild", readonly = true, required = false, defaultValue = "false")
    private Boolean failBuild;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MuleLinter muleLinter = new MuleLinter(appDir, ruleConfiguration, ReportFormat.valueOf(FormatOptionsEnum.CONSOLE.name().toUpperCase()));
        this.getLog().debug(format("Executing linter config %s against application %s", ruleConfiguration.getAbsolutePath(), appDir.getAbsolutePath()));
        RuleExecutor ruleExecutor = muleLinter.buildLinterExecutor();
        try {
            for (FormatOptionsEnum format: formats) {
                this.getLog().info(format("Report formatter found for %s", format));
                FormatterBuilder.build(format, this, ruleExecutor).buildReport();
            }
        } catch (IOException e) {
            getLog().error("Failed to write report", e);
        }
        failIfNeeded(failBuild, ruleExecutor.getResults());
    }
}
