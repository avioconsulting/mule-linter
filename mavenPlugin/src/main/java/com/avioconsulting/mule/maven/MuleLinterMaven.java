package com.avioconsulting.mule.maven;

import com.avioconsulting.mule.MuleLinter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo mule-linter goal
 * Allows to execute mule-linter given the project source directory and groovy rules
 */
@Mojo(name = "mule-linter", defaultPhase = LifecyclePhase.INITIALIZE)
public class MuleLinterMaven extends AbstractMojo {

    @Parameter(property = "ruleConfiguration", readonly = true, required = true)
    private String ruleConfiguration;

    @Parameter(property = "appDir", readonly = true, required = true)
    private String appDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("mule-linter start");
        getLog().info(String.format("RuleConfiguration file: %s appDirectory %s", ruleConfiguration, appDir));
        try {
            MuleLinter ml = new MuleLinter(appDir, ruleConfiguration);
            com.avioconsulting.mule.linter.model.rule.RuleExecutor re = ml.buildLinterExecutor();
            re.getResults().forEach( violation -> getLog().info(violation.getRule().getRuleId().toString() ));

        }catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Validation failed, see log messages", e);
        }

        getLog().info("mule-linter end");
    }
}