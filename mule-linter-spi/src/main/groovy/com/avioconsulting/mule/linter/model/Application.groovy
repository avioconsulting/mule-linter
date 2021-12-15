package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import com.avioconsulting.mule.linter.model.configuration.MuleComponent
import com.avioconsulting.mule.linter.model.pom.PomFile

interface Application {

    List<MuleComponent> getGlobalConfigs()

    List<MuleComponent> findComponents(String componentType, String namespace)

    List<FlowComponent> getFlows()
    List<FlowComponent> getSubFlows()
    List<FlowComponent> getAllFlows()

    List<MuleComponent> getFlowrefs()


    File getApplicationPath()

    PomFile getPomFile()

    GitIgnoreFile getGitignoreFile()

    ReadmeFile getReadmeFile()

    JenkinsFile getJenkinsfile()

    AzurePipelinesFile getAzurePipelinesFile()

    Boolean hasFile(String filename)

    String getName()

    List<PropertyFile> getPropertyFiles()

    MuleArtifact getMuleArtifact()

}