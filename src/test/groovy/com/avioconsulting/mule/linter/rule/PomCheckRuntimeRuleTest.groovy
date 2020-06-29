package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.*

class PomCheckRuntimeRuleTest extends Specification {

    static final APP_NAME = "SampleMuleApp"

    def "Mule Runtime Version Check"(){
        given:
            File appDir = new File(this.getClass().getClassLoader().getResource(APP_NAME).getFile())
            Application app = new Application(appDir)

        when:
            Rule rule = new PomCheckRuntimeRule(version)
            List<RuleViolation> violations = rule.execute(app)

        then:
            violations.size() == size

        where:
            version | size
            "4.2.1" | 0
            "4.2.2" | 1
    }

}
