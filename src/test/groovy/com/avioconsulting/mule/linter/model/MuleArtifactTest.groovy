package com.avioconsulting.mule.linter.model

import spock.lang.Specification

class MuleArtifactTest extends Specification {

    private File appDir
    private File muleArtifactFile

    def setup() {
        appDir = File.createTempDir()
        muleArtifactFile = new File(appDir, MuleArtifact.MULE_ARTIFACT_JSON)
    }

    def cleanup() {
        muleArtifactFile.delete()
        appDir.deleteDir()
    }

    def "MuleArtifact attributes"() {
        given:
        def artifactString = '''{
  "configs": [
    "ch-usage-sync.xml"
  ],
  "redeploymentEnabled": true,
  "name": "ch-usage-sync",
  "minMuleVersion": "4.0.0",
  "requiredProduct": "MULE_EE",
  "classLoaderModelLoaderDescriptor": {
    "id": "mule",
    "attributes": {
      "exportedResources": []
    }
  },
  "bundleDescriptorLoader": {
    "id": "mule",
    "attributes": {}
  },
  "secureProperties": [
    "anypoint.platform.client_id",
    "anypoint.platform.client_secret"
  ]
}'''
        muleArtifactFile.withPrintWriter { pw ->
                pw.print(artifactString)
        }

        when:
        MuleArtifact muleArtifact = new MuleArtifact(appDir)

        then:
        muleArtifact.appName == 'ch-usage-sync'
        muleArtifact.minMuleVersion == '4.0.0'
        muleArtifact.requiredProduct == 'MULE_EE'
        muleArtifact.secureProperties[0] == 'anypoint.platform.client_id'
        muleArtifact.secureProperties[1] == 'anypoint.platform.client_secret'
        muleArtifact.secureProperties.size() == 2
        muleArtifact.configs[0] == 'ch-usage-sync.xml'
        muleArtifact.configs.size() == 1
        muleArtifact.redeploymentEnabled == true
    }

    def "MuleArtifact dynamic attributes"() {
        given:
        def artifactString = '''{
  "configs": [
    "ch-usage-sync.xml"
  ],
  "redeploymentEnabled": true,
  "name": "ch-usage-sync",
  "minMuleVersion": "4.0.0",
  "classLoaderModelLoaderDescriptor": {
    "id": "mule",
    "attributes": {
      "exportedResources": []
    }
  }
}'''
        muleArtifactFile.withPrintWriter { pw ->
            pw.print(artifactString)
        }

        when:
        MuleArtifact muleArtifact = new MuleArtifact(appDir)

        then:
        muleArtifact.appName == 'ch-usage-sync'
        muleArtifact.minMuleVersion == '4.0.0'
        muleArtifact.minMuleVersion.lineNumber == 7
        muleArtifact.requiredProduct == null
        muleArtifact.classLoaderModelLoaderDescriptor.id == 'mule'
        muleArtifact.classLoaderModelLoaderDescriptor.id.lineNumber == 9
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.getLineNumber() == 10
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.exportedResources.size() == 0
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.exportedResources.class.name == 'org.apache.groovy.json.internal.JsonArray'
    }

}
