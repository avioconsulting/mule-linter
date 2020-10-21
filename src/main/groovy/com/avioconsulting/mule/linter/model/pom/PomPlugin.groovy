package com.avioconsulting.mule.linter.model.pom

import groovy.xml.slurpersupport.GPathResult

class PomPlugin extends ArtifactDescriptor {

    PomPlugin(GPathResult pluginXml, PomFile pomFile) {
        super(pluginXml, pomFile)
    }
}

