package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.TestApplication
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.dsl.RulesLoader
import spock.lang.Specification

class GlobalLayoutRuleTest extends Specification {
    private final TestApplication fixture = new TestApplication()

    def setup() { fixture.initialize(); fixture.addPom() }
    def cleanup() { fixture.remove() }

    private void config(String path, String body) {
        new File(fixture.appDir, 'src/main/mule/' + path).parentFile.mkdirs()
        fixture.addFile('src/main/mule/' + path,
            '<mule xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:http="http://www.mulesoft.org/schema/mule/http">' + body + '</mule>')
    }

    def 'split configs and named handlers pass without a required filename'() {
        given:
        config('connections.xml', '<http:listener-config name="listener"/>')
        config('global/global-otel.xml', '<global-property name="env" value="local"/>')
        config('global/errors.xml', '<error-handler name="errors"/>')
        config('orders.xml', '<flow name="orders"/>')
        def app = new MuleApplication(fixture.appDir)
        expect:
        new GlobalConfigSeparationRule().execute(app).empty
        new GlobalFilesNoFlowsRule().execute(app).empty
        RulesLoader.getRuleClassById('GLOBAL_CONFIG_SEPARATION') == GlobalConfigSeparationRule
        RulesLoader.getRuleClassById('GLOBAL_FILES_NO_FLOWS') == GlobalFilesNoFlowsRule
    }

    def 'global paths reject flows and subflows including nested directories'() {
        given:
        config(path, '<' + kind + ' name="processing"/>')
        expect:
        new GlobalFilesNoFlowsRule().execute(new MuleApplication(fixture.appDir)).size() == 1
        where:
        path                         | kind
        'global.xml'                 | 'flow'
        'global-config.xml'          | 'sub-flow'
        'global/health-check.xml'    | 'flow'
        'global/nested/shared.xml'  | 'sub-flow'
    }

    def 'path exception permits shared behavior but not mixed configuration'() {
        given:
        config('global/health-check.xml', '<http:listener-config name="listener"/><flow name="health"/>')
        config('global/global-error-handler.xml', '<error-handler name="errors"/><sub-flow name="format-error"/>')
        def app = new MuleApplication(fixture.appDir)
        def placement = new GlobalFilesNoFlowsRule(exceptions: ['global/health-check.xml', 'global/global-error-handler.xml'])
        expect:
        placement.execute(app).empty
        new GlobalConfigSeparationRule().execute(app).size() == 1
        new GlobalConfigSeparationRule(exceptions: ['global/health-check.xml']).execute(app).empty
    }

    def 'exceptions use exact relative paths and patterns can be extended'() {
        given:
        config('global/nested/health-check.xml', '<flow name="health"/>')
        config('shared/config.xml', '<sub-flow name="process"/>')
        def rule = new GlobalFilesNoFlowsRule(exceptions: ['global/health-check.xml'])
        rule.patterns += 'shared/.*\\.xml'
        expect:
        rule.execute(new MuleApplication(fixture.appDir)).size() == 2
    }
}
