package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.*

class PomCheckMulePluginRuleTest extends Specification {

    static final APP_NAME = "SampleMuleApp"

    def "Mule Maven Plugin Version Check"(){
        given:
            File appDir = new File(this.getClass().getClassLoader().getResource(APP_NAME).getFile())
            Application app = new Application(appDir)

        when:
            Rule rule = new PomCheckMulePluginRule(version)
            List<RuleViolation> violations = rule.execute(app)

        then:
            violations.size() == size

        where:
            version | size
            "3.3.5" | 0
            "3.3.6" | 1
    }

}