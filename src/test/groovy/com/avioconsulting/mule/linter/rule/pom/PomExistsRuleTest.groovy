package com.avioconsulting.mule.linter.rule.pom

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import com.avioconsulting.mule.linter.rule.pom.PomExistsRule
import spock.lang.Specification

class PomExistsRuleTest extends Specification {

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Maven Pom Exists check'() {
        given:
        Rule rule = new PomExistsRule()

        when:
        File appDir = new File(this.class.classLoader.getResource(application).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        application            | size
        'SampleMuleApp'        | 0
        'BadMuleApp'           | 1
    }

}