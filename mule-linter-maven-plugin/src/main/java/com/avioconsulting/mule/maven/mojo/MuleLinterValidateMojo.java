package com.avioconsulting.mule.maven.mojo;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.formatter.CompositeFormatter;
import com.avioconsulting.mule.linter.formatter.FormatterContext;
import com.avioconsulting.mule.linter.model.ReportFormat;
import com.avioconsulting.mule.linter.model.rule.RuleExecutor;
import com.avioconsulting.mule.linter.model.rule.RuleSeverity;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Mojo(name = "validate",
        defaultPhase = LifecyclePhase.VALIDATE)
public class MuleLinterValidateMojo extends AbstractMuleLinterMojo {

    @Parameter(property = "ruleConfiguration", defaultValue = "${basedir}/muleLinter.groovy", readonly = true, required = false)
    private File ruleConfiguration;

    @Parameter(property = "appDir", defaultValue = "${basedir}", readonly = true, required = false)
    private File appDir;

    @Parameter(property = "format", defaultValue = "CONSOLE,JSON", readonly = true, required = false)
    private String formats;

    @Parameter(property = "failBuild", readonly = true, required = false, defaultValue = "false")
    private Boolean failBuild;
    
    @Parameter(property = "failThreshold", readonly = true, required = false, defaultValue = "MAJOR")
    private RuleSeverity failThreshold;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Execute linter
        this.getLog().debug(format("Executing linter config %s against application %s", 
                ruleConfiguration.getAbsolutePath(), appDir.getAbsolutePath()));
        
        MuleLinter muleLinter = new MuleLinter(appDir, ruleConfiguration);
        RuleExecutor ruleExecutor = muleLinter.execute();
        
        // Determine color usage based on Maven's batch mode
        boolean useColor = !this.getLog().isDebugEnabled() && 
                System.getProperty("maven.color.disabled") == null;
        
        // Create formatter context
        FormatterContext context = new FormatterContext(
                getOutputDirectory(),
                useColor,
                failThreshold,
                appDir
        );
        
        // Parse comma-separated formats
        List<ReportFormat> reportFormats = Arrays.stream(formats.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(ReportFormat::valueOf)
                .collect(Collectors.toList());
        
        // Format results
        this.getLog().info(format("Generating reports in formats: %s", formats));
        CompositeFormatter formatter = new CompositeFormatter(reportFormats);
        formatter.format(ruleExecutor, context);
        
        // Fail build if needed
        if (failBuild) {
            failIfNeeded(failThreshold, ruleExecutor.getResults());
        }
    }
}
