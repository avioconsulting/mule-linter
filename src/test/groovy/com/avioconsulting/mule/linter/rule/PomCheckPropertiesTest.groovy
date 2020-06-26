package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.MuleLinter
import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import spock.lang.*

class PomCheckPropertiesTest extends Specification {

    Application app

    void setup() {
        File appDir = new File(this.getClass().getClassLoader().getResource("SampleMuleApp").getFile())
        app = new Application(appDir)

    }


    void cleanup() {

    }

    def "Test"(){
        expect:
        Rule r1 = new PomExistsRule(app)
        r1.setSeverity("SUPER_IMPORTANT")
        r1.execute()

    }

}
