package com.avioconsulting.mule

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

@Mojo(name = 'MuleLinter', defaultPhase = LifecyclePhase.COMPILE)
class MuleLinterMojo extends AbstractMojo {
    @Parameter(defaultValue = '${project.basedir.path}', required = true, readonly = true)
    String appDir

    @Parameter(property = 'muleLinter.config', defaultValue = 'RuleConfiguration.groovy')
    String ruleConfiguration

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        MuleLinter ml = new MuleLinter(appDir, ruleConfiguration)
        ml.runLinter()
    }
}
