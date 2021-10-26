package com.avioconsulting.mule.linter.model

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.configuration.FlowComponent
import spock.lang.Specification

class ConfigurationFileTest extends Specification {

    private final TestApplication testApp = new TestApplication()

    def setup() {
        testApp.initialize()
        testApp.buildConfigContent('business.xml', COMPLEX_CONFIG)
    }

    def cleanup() {
        testApp.remove()
    }

    def 'check nested component of flow'() {
        given:
        Application app = new Application(testApp.appDir)

        when:
        List<FlowComponent> subFlows = app.configurationFiles[0].flows

        then:
        subFlows[0].componentName == 'flow'
        subFlows[0].name == 'nested-component'
        subFlows[0].children[0].componentName == 'until-successful'
        subFlows[0].children[0].children[0].componentName == 'try'
        subFlows[0].children[0].children[0].children[0].componentName == 'logger'
        subFlows[0].children[0].children[0].children[0].level == 'INFO'
    }

    def 'check nested component by find component'() {
        given:
        Application app = new Application(testApp.appDir)

        when:
        List<FlowComponent> untilSucess = app.configurationFiles[0].findComponents('until-successful',
                                                                Namespace.CORE)

        then:
        untilSucess[0].componentName == 'until-successful'
        untilSucess[0].children[0].componentName == 'try'
        untilSucess[0].children[0].children[0].componentName == 'logger'
        untilSucess[0].children[0].children[0].level == 'INFO'
    }

    static final String COMPLEX_CONFIG = '''
\t<flow name="nested-component" doc:id="abe62b2a-41d7-46b3-9fa9-e8835225da8a" >
\t\t<until-successful maxRetries="5" doc:name="Until Successful" doc:id="0ba23691-bf58-4251-a3d6-779c8f9f6ef3" >
\t\t\t<try doc:name="Try" doc:id="c4081cc5-fbf3-4ab5-ace5-bd75bb442bef" >
\t\t\t\t<logger level="INFO" doc:name="Logger" doc:id="d875fc9c-3969-4d2d-b3c8-2efc3d478495" />
\t\t\t</try>
\t\t</until-successful>
\t</flow>
'''

}

