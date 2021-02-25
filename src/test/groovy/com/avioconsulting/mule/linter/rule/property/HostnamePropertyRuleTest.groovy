package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Ignore
import spock.lang.Specification

class HostnamePropertyRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.cleanDirectory('src/main/resources/properties/')
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Hostname as domain name passes rule'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', GOOD_PROPERTY_1)
        Rule rule = new HostnamePropertyRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 1
        violations.size() == 0
    }

    @Ignore
    def 'Hostname as ip address fails rule'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', BAD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.properties', BAD_PROPERTY_2)
        Rule rule = new HostnamePropertyRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 2
        violations[0].fileName.contains('sample-mule-app.test.properties')
        violations[0].message == HostnamePropertyRule.RULE_VIOLATION_MESSAGE + 'db.host'
        violations[1].fileName.contains('sample-mule-app.dev.properties')
        violations[1].message == HostnamePropertyRule.RULE_VIOLATION_MESSAGE + 'db.hostname'
    }

    def 'Exempt properties pass rule'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', BAD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.properties', BAD_PROPERTY_2)
        String[] exemptions = ['db.host','db.hostname']
        Rule rule = new HostnamePropertyRule(exemptions)

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 0
    }

    private static final String GOOD_PROPERTY_1 = '''
db.port = 1521
db.host = mydomain.com
db.user = bill
db.secret = ![abcdef==]
'''

    private static final String BAD_PROPERTY_1 = '''
db.port = 1521
db.host = 123.123.123.123
db.user = bill
db.secret = ![abcdef==]
'''

    private static final String BAD_PROPERTY_2 = '''
db.port = 1521
db.hostname = 127.0.0.1
db.user = bill
db.secret = ![abcdef==]
'''

}
