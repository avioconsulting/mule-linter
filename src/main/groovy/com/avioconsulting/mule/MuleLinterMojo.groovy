package com.avioconsulting.mule

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

@Mojo(name = 'MuleLinter', defaultPhase = LifecyclePhase.COMPILE)
class MuleLinterMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project

    @Parameter(property = "muleLinter.config", defaultValue = "RuleConfiguration.groovy")
    String ruleConfiguration

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        String appDir = project.basedir

        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration)
        ml.runLinter()
    }
}
