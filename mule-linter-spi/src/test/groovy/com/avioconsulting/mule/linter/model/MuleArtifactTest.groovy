package com.avioconsulting.mule.linter.model

import org.apache.groovy.json.internal.JsonArray
import org.apache.groovy.json.internal.JsonBoolean
import org.apache.groovy.json.internal.JsonMap
import org.apache.groovy.json.internal.JsonString
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

    static def toJsonArray(strings, startIdx){
        JsonArray array = new JsonArray(startIdx);
        strings.eachWithIndex { str, idx -> array.add(new JsonString(str, startIdx + idx))}
        return array
    }

    def "MuleArtifact attributes"() {
        given:
        JsonMap jsonMap = new JsonMap(1);
        jsonMap.put("configs", toJsonArray(["ch-usage-sync.xml"], 2))
        jsonMap.put("name",new JsonString("ch-usage-sync", 1))
        jsonMap.put("minMuleVersion",new JsonString('4.0.0',1))
        jsonMap.put("requiredProduct",new JsonString('MULE_EE',1))
        jsonMap.put("secureProperties", toJsonArray(["anypoint.platform.client_id", "anypoint.platform.client_secret"], 1))
        jsonMap.put("configs", toJsonArray(['ch-usage-sync.xml'], 1))
        jsonMap.put("redeploymentEnabled", new JsonBoolean(true,1))

        when:
        MuleArtifact muleArtifact = new MuleArtifact(appDir, jsonMap)

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

    @SuppressWarnings('UnnecessaryGetter')
    def "MuleArtifact dynamic attributes"() {
        given:
        JsonMap jsonMap = new JsonMap(1);
        jsonMap.put("configs", toJsonArray(["ch-usage-sync.xml"], 2))
        jsonMap.put("name",new JsonString("ch-usage-sync", 1))
        jsonMap.put("minMuleVersion",new JsonString('4.0.0',7))

        JsonMap descriptor = new JsonMap(9);
        descriptor.put("id", new JsonString("mule",9))
        JsonMap attributes = new JsonMap(10)
        attributes.put("exportedResources", new JsonArray(11))
        descriptor.put("attributes", attributes)
        jsonMap.put("classLoaderModelLoaderDescriptor", descriptor)

        when:
        MuleArtifact muleArtifact = new MuleArtifact(appDir, jsonMap)

        then:
        muleArtifact.appName == 'ch-usage-sync'
        muleArtifact.minMuleVersion == '4.0.0'
        muleArtifact.minMuleVersion.lineNumber == 7
        muleArtifact.requiredProduct == null
        muleArtifact.classLoaderModelLoaderDescriptor.id == 'mule'
        muleArtifact.classLoaderModelLoaderDescriptor.id.lineNumber == 9
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.getLineNumber() == 10
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.exportedResources.size() == 0
        muleArtifact.classLoaderModelLoaderDescriptor.attributes.exportedResources.class.name ==
                'org.apache.groovy.json.internal.JsonArray'
    }

}
