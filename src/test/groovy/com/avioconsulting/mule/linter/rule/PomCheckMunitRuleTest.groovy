package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.*

class PomCheckMunitRuleTest extends Specification {

    static final APP_NAME = "SampleMuleApp"

    def "Munit Version Check"(){
        given:
            File appDir = new File(this.getClass().getClassLoader().getResource(APP_NAME).getFile())
            Application app = new Application(appDir)

        when:
            Rule rule = new PomCheckMunitRule(version)
            List<RuleViolation> violations = rule.execute(app)

        then:
            violations.size() == size

        where:
            version | size
            "2.2.1" | 0
            "3.2.2" | 1
    }

}
