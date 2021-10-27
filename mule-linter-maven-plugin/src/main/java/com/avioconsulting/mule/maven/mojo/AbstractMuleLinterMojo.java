package com.avioconsulting.mule.maven.mojo;

import com.avioconsulting.mule.linter.model.rule.RuleSeverity;
import com.avioconsulting.mule.linter.model.rule.RuleViolation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

public abstract class AbstractMuleLinterMojo extends AbstractMojo {

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/mule-linter", readonly = true, required = true )
    protected File outputDirectory;


    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    public File getOutputDirectory(){
        return  outputDirectory;
    }

    public String getProjectName (){
        return this.project.getName();
    }

    public String getProjectVersion (){
        return this.project.getVersion();
    }

    public void failIfNeeded(boolean shouldFail, List<RuleViolation> violations){
        if(shouldFail && !violations.isEmpty()
            && violations.stream()
                .anyMatch(ruleViolation ->
                        !ruleViolation.getRule().getSeverity().equals(RuleSeverity.MINOR))){
            throw new RuntimeException("Linter validation has non-minor errors.");
        }
    }
}
