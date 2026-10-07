package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import spock.lang.Specification

class LocalHostnamePolicyTest extends Specification {
    private final TestApplication fixture = new TestApplication()
    def setup() { fixture.initialize(); fixture.addPom() }
    def cleanup() { fixture.remove() }

    def 'local and unit basenames are exempt but deployed environments remain checked'() {
        given:
        new File(fixture.appDir, 'src/main/resources/properties').mkdirs()
        ['local', 'unit', 'dev', 'prod', 'not-local'].each {
            fixture.addFile("src/main/resources/properties/${it}.properties", 'db.host=127.0.0.1')
        }
        def app = new MuleApplication(fixture.appDir)
        expect:
        new HostnamePropertyRule().execute(app).size() == 3
        new HostnamePropertyRule(fileExemptions: []).execute(app).size() == 5
        new HostnamePropertyRule(fileExemptions: ['dev.properties']).execute(app).size() == 4
        new HostnamePropertyRule(exemptions: ['db.host']).execute(app).empty
    }
}
