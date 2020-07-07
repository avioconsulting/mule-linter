package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.parser.JsonSlurper
import org.apache.groovy.json.internal.JsonArray
import org.apache.groovy.json.internal.JsonBoolean
import org.apache.groovy.json.internal.JsonMap
import org.apache.groovy.json.internal.JsonString

class MuleArtifact extends ProjectFile {

    public static final String MULE_ARTIFACT_JSON = 'mule-artifact.json'

    private JsonMap muleArtifact;

    MuleArtifact(File f) {
        super(new File(f, MULE_ARTIFACT_JSON))
        if(!f.isDirectory() && f.exists()) {
            throw new IllegalArgumentException('File must be a Mule application directory that exists')
        }
        parseMuleArtifact()
    }

    private void parseMuleArtifact() {
        JsonSlurper slurper = new JsonSlurper()
        muleArtifact = slurper.parse(file)
    }

    JsonArray getSecureProperties() {
        return muleArtifact.secureProperties
    }

    JsonString getMinMuleVersion() {
        return muleArtifact.minMuleVersion
    }

    JsonString getRequiredProduct() {
        return muleArtifact.requiredProduct
    }

    JsonString getAppName() {
        return muleArtifact.name
    }

    JsonBoolean getRedeploymentEnabled() {
        return muleArtifact.redeploymentEnabled
    }

    JsonArray getConfigs() {
        return muleArtifact.configs
    }

    Object getProperty(String propertyName) {
        def meta = this.metaClass.getMetaProperty(propertyName)
        if(meta) {
            meta.getProperty(this)
        } else {
            return muleArtifact[propertyName]
        }
    }

}
