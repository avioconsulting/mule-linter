package com.avioconsulting.mule.maven;

import com.avioconsulting.mule.MuleLinter;
import org.apache.maven.plugin.AbstractMojo;
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

    public void execute() {
        getLog().info("mule-linter start");
        getLog().info(String.format("RuleConfiguration file: %s appDirectory %s", ruleConfiguration, appDir));
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration);
        ml.runLinter();
        getLog().info("mule-linter end");
    }
}