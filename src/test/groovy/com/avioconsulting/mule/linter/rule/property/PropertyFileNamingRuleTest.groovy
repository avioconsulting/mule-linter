package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.Rule
import com.avioconsulting.mule.linter.model.RuleViolation
import spock.lang.Specification

class PropertyFileNamingRuleTest extends Specification {

    @SuppressWarnings(['MethodName', 'MethodReturnTypeRequired', 'GStringExpressionWithinString'])
    def 'Property File Naming Rule check'() {
        given:
        Rule rule = new PropertyFileNamingRule(['dev', 'test', 'prod'], '${appname}-${env}.properties')

        when:
        File appDir = new File(this.class.classLoader.getResource(application).file)
        Application app = new Application(appDir)
        List<RuleViolation> violations = rule.execute(app)

        then:
        violations.size() == size

        where:
        application     | size
        'SampleMuleApp' | 1
    }

}
