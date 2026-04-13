package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
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
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 0
    }

    def 'YAML Property File with encrypted password'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.yaml', YAML_GOOD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.yaml', YAML_GOOD_PROPERTY_1)
        Rule rule = new EncryptedPasswordRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 0
    }

    def 'Property File not encrypted password'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.properties', BAD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.properties', BAD_PROPERTY_2)
        Rule rule = new EncryptedPasswordRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 3
        RuleViolation testPropertiesViolation = violations.find {
            it.fileName.contains('sample-mule-app.test.properties')
        }
        testPropertiesViolation.message.endsWith('db.secret')
        List<RuleViolation> devPropertiesViolation = violations.findAll {
            it.fileName.contains('sample-mule-app.dev.properties')
        }
        devPropertiesViolation.find {
            it.message.contains('db.secret')
        }
        devPropertiesViolation.find {
            it.message.contains('password')
        }
    }

    def 'YAML Property File with plain text password'() {
        given:
        testApp.addFile('src/main/resources/properties/sample-mule-app.test.yaml', YAML_GOOD_PROPERTY_1)
        testApp.addFile('src/main/resources/properties/sample-mule-app.dev.yaml', YAML_BAD_PROPERTY_1)
        Rule rule = new EncryptedPasswordRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        app.propertyFiles.size() == 2
        violations.size() == 2
        List<RuleViolation> devPropertiesViolation = violations.findAll {
            it.fileName.contains('sample-mule-app.dev.yaml')
        }
        devPropertiesViolation.find {
            it.message.contains('db.secret')
        }
        devPropertiesViolation.find {
            it.message.contains('password')
        }
    }

    private static final String GOOD_PROPERTY_1 = '''
user=jallen
password=![abcdef==]

db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
other.secret = ${secure::shared.secret}
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
    private static final String YAML_GOOD_PROPERTY_1 = '''
user: "james"
password: "![abcdef==]"

db:
  port: 1521
  host: "localhost"
  user: "areed"
  secret: "![abcdef==]"

other:
  secret = ${secure::shared.secret}
'''
    private static final String YAML_BAD_PROPERTY_1 = '''
user: "jallen"
password: "mypassword"

db:
  port: 1521
  host: "localhost"
  user: "areed"
  secret: "BillsRule!"
'''
}
