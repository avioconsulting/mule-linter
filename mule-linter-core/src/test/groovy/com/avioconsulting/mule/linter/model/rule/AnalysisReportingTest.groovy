package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.MuleLinter
import com.avioconsulting.mule.linter.model.MuleApplication
import com.avioconsulting.mule.linter.model.ReportFormat
import com.avioconsulting.mule.linter.rule.FileExistsRule
import com.avioconsulting.mule.linter.resolver.ParentPomResolver
import com.google.gson.Gson
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

class AnalysisReportingTest extends Specification {
    @TempDir File directory

    def 'writers are immutable, order independent, idempotent and preserve legacy JSON and XML'() {
        given:
        def rule = new FileExistsRule(path: 'missing', severity: RuleSeverity.MAJOR)
        def exe = new RuleExecutor(new MuleApplication(directory), [new RuleSet(rules: [rule])])
        exe.executeRules()
        def violation = exe.results[0]
        // Exercise absolute-path conversion, which previously mutated the violation.
        violation.fileName = new File(directory, 'missing').absolutePath
        def result = new AnalysisResult([AnalysisFinding.snapshot(violation)], [], 1, directory.absolutePath)
        def originalJson = new Gson().toJson(violation)
        def expected = ReportFormat.values().collectEntries { [(it): render(result, it)] }
        assert violation.fileName == new File(directory, 'missing').absolutePath
        assert violation.message == 'A missing file does not exist'
        assert rule.severity == RuleSeverity.MAJOR

        when:
        ReportFormat.values().reverseEach { assert render(result, it) == expected[it] }
        rule.ruleName = 'changed'
        rule.severity = RuleSeverity.BLOCKER
        violation.fileName = 'changed'
        violation.message = 'changed'

        then:
        ReportFormat.values().every { render(result, it) == expected[it] }
        result.findings[0].legacyJson == originalJson
        new JsonSlurper().parseText(expected[ReportFormat.JSON]) == [issues: [[
                engineId: FileExistsRule.RULE_NAME, ruleId: FileExistsRule.RULE_ID, severity: 'MAJOR',
                type: 'CODE_SMELL', primaryLocation: [message: 'A missing file does not exist', filePath: 'missing']]]]
        expected[ReportFormat.XML].contains('<ruleId>FILE_EXISTS</ruleId>')
        expected[ReportFormat.XML].contains('<path>missing</path>')
        expected[ReportFormat.XML] == ReportWriters.convertToXML(
                '<?xml version="1.0" encoding="ISO-8859-15"?>\n<violations>' +
                org.json.XML.toString(new org.json.JSONTokener(originalJson).nextValue(), 'violation') + '</violations>')
        expected[ReportFormat.CONSOLE].contains(new File(directory, 'missing').absolutePath)

        when:
        result.findings.clear()
        then:
        thrown(UnsupportedOperationException)

        when:
        result.findings[0].message = 'mutation'
        then:
        thrown(ReadOnlyPropertyException)
    }

    def 'executor repetition replaces counts and findings without changing previous snapshot'() {
        given:
        def rule = new FileExistsRule(path: 'missing')
        def exe = new RuleExecutor(new MuleApplication(directory), [new RuleSet(rules: [rule])])
        exe.executeRules()
        def first = exe.analysisResult

        when:
        exe.executeRules()
        then:
        exe.ruleCount == 1
        exe.results.size() == 1
        exe.analysisResult.findings.size() == 1
        !exe.analysisResult.is(first)

        when:
        new File(directory, 'missing').text = 'present'
        exe.executeRules()
        then:
        exe.analysisResult.findings.empty
        first.findings.size() == 1
    }

    def 'JSON preserves startLine and only trims an application path prefix'() {
        given:
        def rule = new FileExistsRule()
        def finding = AnalysisFinding.snapshot(new RuleViolation(rule, 'other/' + directory.absolutePath + '/file.xml', 12, 'message'))
        def result = new AnalysisResult([finding], [], 1, directory.absolutePath)
        expect:
        new JsonSlurper().parseText(render(result, ReportFormat.JSON)).issues[0].primaryLocation == [
                message: 'message', filePath: finding.fileName, textRange: [startLine: 12]]
    }

    def 'explicit severity matrix and report only defaults'() {
        expect:
        RuleSeverity.values().each { severity ->
            RuleSeverity.values().each { threshold ->
                def ranks = [BLOCKER: 4, CRITICAL: 3, MAJOR: 2, MINOR: 1]
                def finding = AnalysisFinding.snapshot(new RuleViolation(new FileExistsRule(severity: severity), 'file', 1, 'message'))
                def result = new AnalysisResult([finding], [], 1, directory.absolutePath)
                assert FailurePolicy.fails(result, true, threshold) == (ranks[severity.name()] >= ranks[threshold.name()])
                assert !FailurePolicy.fails(result, false, threshold)
                assert FailurePolicy.fails(result, true) == (severity != RuleSeverity.MINOR)
            }
        }
    }

    def 'warnings mark incomplete analysis separately, strict policy is optional'() {
        given:
        def app = new MuleApplication(directory)
        app.analysisWarnings.add('Could not resolve parent POM chain: offline')
        def exe = new RuleExecutor(app, [])
        exe.executeRules()

        expect:
        !exe.analysisResult.complete
        exe.analysisResult.findings.empty
        !FailurePolicy.fails(exe.analysisResult, true)
        render(exe.analysisResult, ReportFormat.CONSOLE).contains('Analysis incomplete.')
        render(exe.analysisResult, ReportFormat.JSON).contains('"issues": []')

        when:
        FailurePolicy.requireComplete(exe.analysisResult, false)
        FailurePolicy.requireComplete(exe.analysisResult, true)
        then:
        thrown(IllegalStateException)
    }

    def 'unresolved parents are captured in analysis rather than as violations'() {
        given:
        new File(directory, 'pom.xml').text = '''<project><modelVersion>4.0.0</modelVersion>
            <parent><groupId>test</groupId><artifactId>parent</artifactId><version>1</version></parent>
            <artifactId>child</artifactId></project>'''
        def resolver = ParentPomResolver.getInstance()
        def original = resolver.metaClass
        resolver.metaClass.resolveParentChain = { File pom -> throw new IllegalStateException('offline fixture') }

        when:
        def app = new MuleApplication(directory)
        def exe = new RuleExecutor(app, [])
        exe.executeRules()
        then:
        !exe.analysisResult.complete
        exe.analysisResult.warnings[0].contains('Could not resolve parent POM chain')
        exe.analysisResult.warnings[0].contains('offline fixture')
        exe.analysisResult.findings.empty

        cleanup:
        resolver.metaClass = original
    }

    def 'requested report failures propagate without closing caller stream'() {
        given:
        def stream = new OutputStream() {
            boolean closed
            void write(int value) { throw new IOException('disk full') }
            void close() { closed = true }
        }
        when:
        ReportWriters.write(new AnalysisResult([], [], 0, directory.absolutePath), ReportFormat.JSON, stream)
        then:
        thrown(IOException)
        !stream.closed
    }

    def 'invalid configuration is detected before loading application'() {
        given:
        def config = new File(directory, 'rules.groovy')
        config.text = 'mule_linter { rules { TYPO_RULE } }'
        when:
        new MuleLinter(new File(directory, 'nonexistent'), config, ReportFormat.CONSOLE)
        then:
        def error = thrown(IllegalArgumentException)
        error.message.contains('TYPO_RULE')
    }

    private static String render(AnalysisResult result, ReportFormat format) {
        def stream = new ByteArrayOutputStream()
        ReportWriters.write(result, format, stream)
        stream.toString('UTF-8')
    }
}
