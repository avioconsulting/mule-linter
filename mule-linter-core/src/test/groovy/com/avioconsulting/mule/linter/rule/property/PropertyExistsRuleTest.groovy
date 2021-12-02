package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import spock.lang.Specification

@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired',
        'GStringExpressionWithinString', 'StaticFieldsBeforeInstanceFields'])
class PropertyExistsRuleTest extends Specification {

    private static final String PROPERTY_DIRECTORY = 'src/main/resources/properties/'
    private final TestApplication testApp = new TestApplication()
    private MuleApplication app

    def setup() {
        testApp.initialize()
        testApp.addPom()
        testApp.cleanDirectory(PROPERTY_DIRECTORY)
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-dev.properties', GOOD_PROPERTY_1)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'Property Exists'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-prod.properties', GOOD_PROPERTY_1)
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', GOOD_PROPERTY_1)
        Rule rule = new PropertyExistsRule('sample.property')

        when:
        app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Property Exists with env list'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', GOOD_PROPERTY_1)
        Rule rule = new PropertyExistsRule()
        rule.propertyName = 'sample.property'
        rule.environments = ['dev', 'test']

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Property Exists with env list and pattern'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'prod.properties', GOOD_PROPERTY_1)
        Rule rule = new PropertyExistsRule()
        rule.propertyName = 'sample.property'
        rule.environments = ['prod']
        rule.pattern = '${env}.properties'

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Property Missing'() {
        given:
        testApp.addFile(PROPERTY_DIRECTORY + 'sample-mule-app-test.properties', MISSING_PROPERTY)
        Rule rule = new PropertyExistsRule()
        rule.propertyName = 'sample.property'
        rule.environments = ['dev', 'test']

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.endsWith('sample-mule-app-test.properties')
        violations[0].message.endsWith('sample.property')
        violations[0].rule.ruleId == PropertyExistsRule.RULE_ID
    }

    def 'Property File Missing'() {
        given:
        Rule rule = new PropertyExistsRule()
        rule.propertyName = 'sample.property'
        rule.environments = ['dev', 'test']

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 1
        violations[0].fileName.endsWith('sample-mule-app-test.properties')
        violations[0].message.endsWith('sample-mule-app-test.properties')
        violations[0].rule.ruleId == PropertyExistsRule.RULE_ID
    }

    private static final String GOOD_PROPERTY_1 = '''
user=jallen
password=![abcdef==]
sample.property = AVIORocks!
db.port = 1521
db.host = localhost
db.user = areed
db.secret = ![abcdef==]
'''

    private static final String MISSING_PROPERTY = '''
user=jallen
password= ![abcdef==]

db.port = 1521
db.host = localhost
db.user = areed
db.secret = BillsRule!
'''

}
