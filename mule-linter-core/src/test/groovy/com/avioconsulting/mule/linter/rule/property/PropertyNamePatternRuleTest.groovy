package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

class PropertyNamePatternRuleTest extends Specification {

    private static final String PROPERTY_DIRECTORY = 'src/main/resources/properties/'
    private final TestApplication testApp = new TestApplication()
    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.cleanDirectory(PROPERTY_DIRECTORY)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Default properties naming convention check success'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', GOOD_PROPERTY)
        Rule rule = new PropertyNamePatternRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'JAVA_PROPERTIES_CASE Properties naming convention check success'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', GOOD_PROPERTY)
        Rule rule = new PropertyNamePatternRule()
        rule.format ='JAVA_PROPERTIES_CASE'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'CamelCase Properties naming convention check success'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', GOOD_PROPERTY_1)
        Rule rule = new PropertyNamePatternRule()
        rule.format ='CAMEL_CASE'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Properties naming with kebab-case, snake_case convention check failure'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', BAD_PROPERTY)
        Rule rule = new PropertyNamePatternRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == 'Properties name is not following naming conventions: db_host'
        violations[1].message == 'Properties name is not following naming conventions: db-port'
    }

    def 'Default properties naming convention for YAML properties file check success'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.yaml', GOOD_PROPERTY_YAML)
        Rule rule = new PropertyNamePatternRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Properties naming convention with kebab-case, snake_case for YAML properties file check failure'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'dev.yaml', BAD_PROPERTY_YAML)
        Rule rule = new PropertyNamePatternRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 2
        violations[0].message == 'Properties name is not following naming conventions: db.user_password'
        violations[1].message == 'Properties name is not following naming conventions: db.user-name'
    }
    private static final String GOOD_PROPERTY = '''
user.name=jallen
user.password=![abcdef==]
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''
    private static final String GOOD_PROPERTY_1 = '''
userName=jallen
userPassword=![abcdef==]
dbPort = 1521
dbHost = localhost
dbUser = areed
dbSecret = ![abcdef==]
'''


    private static final String BAD_PROPERTY = '''
user=jallen
password= ![abcdef==]
db-port = 1521
db_host = localhost
db.user = areed
db.secret = BillsRule!
'''
    private static final GOOD_PROPERTY_YAML = '''
user: james
password: "![abcdef==]"
sample:
  property: BillsRule!
db:
  port: 1521
  host: localhost
  username: areed
  secret: BillsRule!
'''
    private static final BAD_PROPERTY_YAML = '''
user: james
password: "![abcdef==]"
sample:
  property: BillsRule!
db:
  port: 1521
  host: localhost
  user-name: areed
  user_password: BillsRule!
'''
}
