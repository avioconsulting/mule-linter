package com.avioconsulting.mule.linter.model.pom

import groovy.xml.slurpersupport.GPathResult

class PomDependency extends ArtifactDescriptor {

    PomDependency(GPathResult pluginXml, PomFile pomFile) {
        super(pluginXml, pomFile)
    }
}
