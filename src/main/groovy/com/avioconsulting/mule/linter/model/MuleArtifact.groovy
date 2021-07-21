package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.parser.JsonSlurper
import org.apache.groovy.json.internal.JsonArray
import org.apache.groovy.json.internal.JsonBoolean
import org.apache.groovy.json.internal.JsonMap
import org.apache.groovy.json.internal.JsonString

class MuleArtifact extends ProjectFile {

    public static final String MULE_ARTIFACT_JSON = 'mule-artifact.json'

    private JsonMap muleArtifact
    private final Boolean exists

    MuleArtifact(File f) {
        super(new File(f, MULE_ARTIFACT_JSON))

        File file = new File(f, MULE_ARTIFACT_JSON)

        if (file.exists()) {
            parseMuleArtifact(file)
            this.exists = true
        } else {
            this.exists = false
        }
    }

    JsonArray getSecureProperties() {
        return muleArtifact.secureProperties
    }
/*
    JsonString getMinMuleVersion() {
        return muleArtifact.minMuleVersion
    }
*/
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
        MetaProperty meta = this.getMetaClass().getMetaProperty(propertyName)
        if (meta) {
            meta.getProperty(this )
        } else {
            return muleArtifact[propertyName]
        }
    }

    private void parseMuleArtifact(File file) {
        JsonSlurper slurper = new JsonSlurper()
        muleArtifact = slurper.parse(file) as JsonMap
    }

}
