package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.rule.pom.MunitVersionRule
import spock.lang.Specification

class MunitVersionRuleTest extends Specification {

    private static final String APP_NAME = 'SampleMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Munit Version Check'() {
        given:
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        Application app = new Application(appDir)

        when:
        Rule rule = new MunitVersionRule(version)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '2.2.1' | 0
        '3.2.2' | 1
    }

}
