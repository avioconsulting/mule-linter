package com.avioconsulting.mule.maven.mojo;

import com.avioconsulting.mule.MuleLinter;
import com.avioconsulting.mule.linter.model.ReportFormat;
import com.avioconsulting.mule.linter.model.rule.RuleExecutor;
import com.avioconsulting.mule.linter.model.rule.RuleSeverity;
import com.avioconsulting.mule.linter.model.rule.FailurePolicy;
import com.avioconsulting.mule.maven.formatter.FormatOptionsEnum;
import com.avioconsulting.mule.maven.formatter.FormatterBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
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

    @Parameter(property = "failureThreshold", defaultValue = "MAJOR")
    private RuleSeverity failureThreshold = RuleSeverity.MAJOR;

    @Parameter(property = "strictAnalysis", defaultValue = "false")
    private boolean strictAnalysis;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RuleExecutor ruleExecutor;
        try {
            if (failureThreshold == null || formats == null || formats.contains(null)) {
                throw new IllegalArgumentException("Valid formats and failureThreshold are required");
            }
            MuleLinter muleLinter = new MuleLinter(appDir, ruleConfiguration, ReportFormat.CONSOLE);
            this.getLog().debug(format("Executing linter config %s against application %s", ruleConfiguration.getAbsolutePath(), appDir.getAbsolutePath()));
            ruleExecutor = muleLinter.buildLinterExecutor();
            for (FormatOptionsEnum format: formats) {
                this.getLog().info(format("Report formatter found for %s", format));
                FormatterBuilder.build(format, this, ruleExecutor).buildReport();
            }
            if (!formats.contains(FormatOptionsEnum.CONSOLE)) {
                for (String warning : ruleExecutor.getAnalysisResult().getWarnings()) {
                    getLog().warn("Analysis warning: " + warning);
                }
                if (!ruleExecutor.getAnalysisResult().isComplete()) getLog().warn("Analysis incomplete.");
            }
            FailurePolicy.requireComplete(ruleExecutor.getAnalysisResult(), strictAnalysis);
        } catch (Exception e) {
            throw new MojoExecutionException("Linter configuration, execution or reporting failed: " + e.getMessage(), e);
        }
        if (FailurePolicy.fails(ruleExecutor.getAnalysisResult(), Boolean.TRUE.equals(failBuild), failureThreshold)) {
            throw new MojoFailureException("Linter findings meet or exceed " + failureThreshold + ".");
        }
    }
}
