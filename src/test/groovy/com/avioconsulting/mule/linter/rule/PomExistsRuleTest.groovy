package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification


class PomExistsRuleTest extends Specification {

    def "Maven Pom Exists check"(){
        given:
            File appDir = new File(this.getClass().getClassLoader().getResource(APP_NAME).getFile())
            Application app = new Application(appDir)

        when:
            Rule rule = new PomExistsRule()
            List<RuleViolation> violations = rule.execute(app)

        then:
            violations.size() == size

        where:
            APP_NAME                  | size
            "SampleMuleApp"           | 0
            "multi-module-project"    | 1
    }

}