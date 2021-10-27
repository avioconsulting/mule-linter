package com.avioconsulting.mule.linter.model

import org.apache.groovy.json.internal.JsonArray
import org.apache.groovy.json.internal.JsonBoolean
import org.apache.groovy.json.internal.JsonMap
import org.apache.groovy.json.internal.JsonString

class MuleArtifact extends ProjectFile {

    public static final String MULE_ARTIFACT_JSON = 'mule-artifact.json'

    private JsonMap muleArtifact
    private final Boolean exists

    MuleArtifact(File f, JsonMap content) {
        super(f)
        if (file.exists()) {
            muleArtifact = content
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


}
