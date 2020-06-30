package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class PomCheckRuntimeRuleTest extends Specification {

    private static final String APP_NAME = 'SampleMuleApp'

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired'])
    def 'Mule Runtime Version Check'() {
        given:
        File appDir = new File(this.class.classLoader.getResource(APP_NAME).file)
        Application app = new Application(appDir)

        when:
        Rule rule = new PomCheckRuntimeRule(version)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        version | size
        '4.2.1' | 0
        '4.2.2' | 1
    }

}
