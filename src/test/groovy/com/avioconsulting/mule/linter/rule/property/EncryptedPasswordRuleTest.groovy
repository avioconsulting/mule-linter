package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Ignore
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
class EncryptedPasswordRuleTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.cleanDirectory('src/main/resources/properties/')
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Property File encrypted password'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', GOOD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.properties', GOOD_PROPERTY_1)
        Rule rule = new EncryptedPasswordRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 0
    }

    @Ignore
    def 'Property File not encrypted password'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', BAD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.properties', BAD_PROPERTY_2)
        Rule rule = new EncryptedPasswordRule()

        when:
        Application app = new Application(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 3
        violations[0].fileName.contains('sample-mule-app.test.properties')
        violations[0].message.endsWith('db.secret')
        violations[1].fileName.contains('sample-mule-app.dev.properties')
        violations[1].message.endsWith('db.secret')
        violations[2].message.endsWith('password')
    }

    private static final String GOOD_PROPERTY_1 = '''
user=jallen
password=![abcdef==]

db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''

    private static final String BAD_PROPERTY_1 = '''
user=jallen
password= ![abcdef==]

db.port = 1521
db.host = localhost
db.user = areed
db.secret = BillsRule!
'''

    private static final String BAD_PROPERTY_2 = '''
user=jallen
password=mypassword

db.port = 1521
db.host = localhost
db.user = areed
db.secret = BillsRule!
'''

}