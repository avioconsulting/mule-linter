package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class MuleMavenPluginVersionRuleTest extends Specification {

    private static final String APP_NAME = 'SampleMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Mule Maven Plugin Version Check'() {
        given:
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        Application app = new Application(appDir)

        when:
        Rule rule = new MuleMavenPluginVersionRule(version)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '3.3.5' | 0
        '3.3.6' | 1
    }

}
