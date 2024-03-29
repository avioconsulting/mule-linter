package com.avioconsulting.mule.linter.model.pom

import com.avioconsulting.mule.linter.model.ProjectFile
import groovy.xml.slurpersupport.GPathResult

class PomFile extends ProjectFile {

    public static final String POM_XML = 'pom.xml'
    static final String PROPERTIES = 'properties'
    private final GPathResult pomXml
    private final Boolean exists

    PomFile(File file, GPathResult pomXml) {
        super(file)
        if (file.exists()) {
            exists = true
            this.pomXml = pomXml
        } else {
            exists = false
            this.pomXml = null
        }
    }

    PomFile(File application, String fileName, GPathResult pomXml) {
        this(new File(application, fileName), pomXml)
    }

    String getArtifactId() {
        return exists ? pomXml.getProperty('artifactId') : ''
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId
    }

    Boolean doesExist() {
        return exists
    }

    String getPath() {
        return file.absolutePath
    }

    PomElement getPomProperty(String propertyName) throws IllegalArgumentException {
        GPathResult p = pomProperties[propertyName] as GPathResult
        if (p == null) {
            throw new IllegalArgumentException('Property doesn\'t exist')
        }

        PomElement prop = new PomElement()
        prop.name = propertyName
        prop.value = p.text()
        prop.lineNo = ArtifactDescriptor.getNodeLineNumber(p)
        return prop
    }
    Integer getPropertiesLineNo() {
        return ArtifactDescriptor.getNodeLineNumber(pomProperties)
    }
    PomPlugin getPlugin(String groupId, String artifactId) {
        PomPlugin plugin
        GPathResult pluginPath = pomXml.build.plugins.plugin.find {
            it.groupId == groupId && it.artifactId == artifactId
        } as GPathResult

        if (pluginPath != null && pluginPath.size() > 0) {
            plugin = new PomPlugin(pluginPath, this)
        }
        return plugin
    }

    PomDependency getDependency(String groupId, String artifactId) {
        PomDependency dependency
        GPathResult dependencyPath = pomXml.dependencies.dependency.find {
            it.groupId == groupId && it.artifactId == artifactId
        } as GPathResult

        if (dependencyPath != null && dependencyPath.size() > 0) {
            dependency = new PomDependency(dependencyPath, this)
        }
        return dependency
    }

    MunitMavenPlugin getMunitPlugin() {
        PomPlugin pp = getPlugin(MunitMavenPlugin.GROUP_ID, MunitMavenPlugin.ARTIFACT_ID)
        return pp == null ? null : new MunitMavenPlugin(pp.pluginXml, this)
    }

    private GPathResult getPomProperties() {
        return pomXml[PROPERTIES] as GPathResult
    }

}
