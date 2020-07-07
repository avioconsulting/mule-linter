package com.avioconsulting.mule.linter.parser

import org.apache.groovy.json.internal.JsonBoolean
import org.apache.groovy.json.internal.JsonMap
import org.apache.groovy.json.internal.JsonString
import spock.lang.Specification

class JsonSlurperTest extends Specification {

    private static final String APP_NAME = "SampleMuleApp"

    def "String values with line numbers"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        File json = new File(new File(this.class.classLoader.getResource(APP_NAME).file), 'mule-artifact.json')

        when:
        JsonMap muleArtifact = slurper.parse(json)
        JsonString minMuleVersion = muleArtifact['minMuleVersion']

        then:
        minMuleVersion == '4.2.2'
        minMuleVersion.lineNumber == 2
        muleArtifact.getClass().getName() == 'org.apache.groovy.json.internal.JsonMap'
        minMuleVersion.getClass().getName() == 'org.apache.groovy.json.internal.JsonString'
    }

    def "Boolean values with line numbers"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        File json = new File(new File(this.class.classLoader.getResource(APP_NAME).file), 'mule-artifact.json')

        when:
        JsonMap muleArtifact = slurper.parse(json)
        JsonBoolean itsTrue = muleArtifact.itsTrue
        JsonBoolean itsFalse = muleArtifact.itsFalse

        then:
        itsTrue == true
        itsTrue.lineNumber == 12
        itsFalse == false
        itsFalse.lineNumber == 13
    }

    def "Null value with line numbers"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        File json = new File(new File(this.class.classLoader.getResource(APP_NAME).file), 'mule-artifact.json')

        when:
        JsonMap muleArtifact = slurper.parse(json)

        then:
        muleArtifact.itsNull.value == null
        muleArtifact.itsNull.lineNumber == 14
    }

    def "Number values with line numbers"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        File json = new File(new File(this.class.classLoader.getResource(APP_NAME).file), 'mule-artifact.json')

        when:
        JsonMap muleArtifact = slurper.parse(json)

        then:
        muleArtifact.itsNumber == 100
        muleArtifact.itsNumber.lineNumber == 15
        muleArtifact.itsDecimal == 100.5
        muleArtifact.itsDecimal.lineNumber == 16
    }

    def "Array values with line numbers and contains"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        File json = new File(new File(this.class.classLoader.getResource(APP_NAME).file), 'mule-artifact.json')

        when:
        JsonMap muleArtifact = slurper.parse(json)
        ArrayList t = new ArrayList();

        then:
        muleArtifact.secureProperties.size() == 7
        muleArtifact.secureProperties[0].lineNumber == 4
        muleArtifact.secureProperties[0] == 'anypoint.platform.client_id'
        muleArtifact.secureProperties[1].lineNumber == 5
        muleArtifact.secureProperties[1] == 'anypoint.platform.client_secret'
        muleArtifact.secureProperties.contains('anypoint.platform.client_id')
        muleArtifact.secureProperties.containsAll(['anypoint.platform.client_id', 'anypoint.platform.client_secret'])
        muleArtifact.secureProperties.contains(10)
        muleArtifact.secureProperties.contains(null)
        muleArtifact.secureProperties.contains(true)
        muleArtifact.secureProperties.contains(false)
    }
}
