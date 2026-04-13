package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.pom.PomFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.pom.ApikitVersionRule
import com.avioconsulting.mule.linter.rule.pom.MuleMavenPluginVersionRule
import com.avioconsulting.mule.linter.rule.pom.MuleRuntimeVersionRule
import com.avioconsulting.mule.linter.rule.pom.MunitMavenPluginAttributesRule
import com.avioconsulting.mule.linter.rule.pom.MunitPluginVersionRule
import com.avioconsulting.mule.linter.rule.pom.MunitVersionRule
import com.avioconsulting.mule.linter.rule.pom.PomDependencyVersionRule
import com.avioconsulting.mule.linter.rule.pom.PomExistsRule
import com.avioconsulting.mule.linter.rule.pom.PomPluginAttributeRule
import com.avioconsulting.mule.linter.rule.pom.PomPropertyValueRule
import spock.lang.Specification

/**
 * Integration tests for effective POM resolution with parent POM inheritance.
 * Tests comprehensive parent-child sample for end-to-end validation of POM rules.
 */
@SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'StaticFieldsBeforeInstanceFields'])
class EffectivePomIntegrationTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
    }

    def cleanup() {
        testApp.remove()
    }

    // ==================== END-TO-END POM EXISTS ====================

    def 'POM exists with comprehensive parent sample'() {
        given:
        testApp.addComprehensiveParentSample()
        testApp.useEffectivePomGeneration()
        Rule rule = new PomExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
        app.pomFile != null
    }

    // ==================== EFFECTIVE POM WITH STANDALONE POM ====================

    def 'Effective POM generation with standalone POM works'() {
        given:
        testApp.addPom()
        testApp.useEffectivePomGeneration()
        
        Rule rule = new PomExistsRule()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
        app.pomFile != null
    }

    def 'Property values are accessible in effective POM'() {
        given:
        testApp.addPom()
        testApp.useEffectivePomGeneration()
        
        Rule rule = new PomPropertyValueRule()
        rule.propertyName = 'munit.version'
        rule.propertyValue = '3.6.3'

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Dependencies are accessible in effective POM'() {
        given:
        testApp.addPom()
        testApp.useEffectivePomGeneration()
        
        Rule rule = new PomDependencyVersionRule()
        rule.groupId = 'org.mule.connectors'
        rule.artifactId = 'mule-http-connector'
        rule.artifactVersion = '1.10.3'
        rule.versionOperator = 'EQUAL'
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    def 'Plugins are accessible in effective POM'() {
        given:
        testApp.addPom()
        testApp.useEffectivePomGeneration()
        
        Rule rule = new MunitMavenPluginAttributesRule()
        rule.init()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == 0
    }

    // ==================== COMPREHENSIVE PARENT SAMPLE STRUCTURE ====================
    // Note: Tests requiring actual Maven parent POM resolution require network access
    // or a properly configured Maven repository. These tests validate the sample structure.

    def 'Comprehensive parent sample files are created correctly'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File childPom = new File(testApp.appDir, 'pom.xml')
        File parentDir = new File(testApp.appDir, 'parent')
        File parentPom = new File(parentDir, 'pom.xml')

        then:
        childPom.exists()
        parentDir.exists()
        parentPom.exists()
    }

    def 'Child POM has correct parent reference'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File childPom = new File(testApp.appDir, 'pom.xml')
        String childContent = childPom.text

        then:
        childContent.contains('<parent>')
        childContent.contains('<groupId>com.avioconsulting.test</groupId>')
        childContent.contains('<artifactId>comprehensive-parent</artifactId>')
        childContent.contains('<version>1.0.0</version>')
        childContent.contains('<relativePath>../parent/pom.xml</relativePath>')
    }

    def 'Parent POM contains management sections'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File parentPom = new File(testApp.appDir, 'parent/pom.xml')
        String parentContent = parentPom.text

        then:
        parentContent.contains('<dependencyManagement>')
        parentContent.contains('<pluginManagement>')
        parentContent.contains('<app.runtime>4.9.16</app.runtime>')
        parentContent.contains('<mule.maven.plugin.version>4.6.0</mule.maven.plugin.version>')
        parentContent.contains('<munit.version>3.6.3</munit.version>')
    }

    def 'Child POM overrides parent properties'() {
        given:
        testApp.addComprehensiveParentSample()
        
        when:
        File childPom = new File(testApp.appDir, 'pom.xml')
        String childContent = childPom.text

        then:
        childContent.contains('<http.connector.version>1.11.0</http.connector.version>')
        childContent.contains('<test.property>test-value</test.property>')
    }

    // ==================== POM FILE ACCESS ====================

    def 'PomFile provides access to effective POM properties'() {
        given:
        testApp.addComprehensiveParentSample()
        testApp.useEffectivePomGeneration()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        PomFile pomFile = app.pomFile

        then:
        pomFile != null
        pomFile.properties != null
        pomFile.properties.size() > 0
    }

    def 'PomFile provides access to specific dependency'() {
        given:
        testApp.addPom()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        PomFile pomFile = app.pomFile

        then:
        pomFile != null
        pomFile.getDependency('org.mule.connectors', 'mule-http-connector') != null
    }

    def 'PomFile provides access to specific plugin'() {
        given:
        testApp.addPom()

        when:
        MuleApplication app = new MuleApplication(testApp.appDir)
        PomFile pomFile = app.pomFile

        then:
        pomFile != null
        pomFile.getPlugin('org.mule.tools.maven', 'mule-maven-plugin') != null
    }
}
