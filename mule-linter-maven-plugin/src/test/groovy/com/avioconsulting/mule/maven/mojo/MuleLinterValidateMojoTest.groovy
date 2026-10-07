package com.avioconsulting.mule.maven.mojo

import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.maven.formatter.FormatOptionsEnum
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import spock.lang.Specification
import spock.lang.TempDir

class MuleLinterValidateMojoTest extends Specification {
    @TempDir File directory
    File config
    MuleLinterValidateMojo mojo
    List<String> lines

    def setup() {
        config = new File(directory, 'rules.groovy')
        config.text = "mule_linter { rules { FILE_EXISTS { path = 'missing'; severity = 'MAJOR' } } }"
        mojo = new MuleLinterValidateMojo()
        set('appDir', directory)
        set('ruleConfiguration', config)
        set('formats', [FormatOptionsEnum.CONSOLE, FormatOptionsEnum.JSON])
        mojo.outputDirectory = new File(directory, 'reports')
        lines = []
        def log = Mock(Log)
        log.info(_ as CharSequence) >> { CharSequence line -> lines.add(line.toString()) }
        log.warn(_ as CharSequence) >> { CharSequence line -> lines.add(line.toString()) }
        mojo.log = log
    }

    def 'default is report only and report filename and structure remain unchanged'() {
        when:
        mojo.execute()
        then:
        new File(mojo.outputDirectory, 'mule-linter-report.json').text.contains('"issues": [')
        lines.any { it.contains('Found a total of 1 violations of 1 rules.') }
    }

    def 'enforced findings use MojoFailureException at default MAJOR threshold'() {
        given:
        set('failBuild', true)
        when:
        mojo.execute()
        then:
        thrown(MojoFailureException)
        new File(mojo.outputDirectory, 'mule-linter-report.json').isFile()
    }

    def 'threshold configuration supports minor and blocker without changing report only'() {
        given:
        config.text = "mule_linter { rules { FILE_EXISTS { path = 'missing'; severity = 'MINOR' } } }"
        set('failBuild', true)
        when:
        mojo.execute()
        set('failureThreshold', RuleSeverity.BLOCKER)
        mojo.execute()
        set('failureThreshold', RuleSeverity.MINOR)
        set('failBuild', false)
        mojo.execute()
        set('failBuild', true)
        mojo.execute()
        then:
        thrown(MojoFailureException)
    }

    def 'invalid configuration maps to MojoExecutionException before application loading'() {
        given:
        config.text = 'mule_linter { rules { UNKNOWN_RULE } }'
        set('appDir', new File(directory, 'absent'))
        when:
        mojo.execute()
        then:
        def error = thrown(MojoExecutionException)
        error.message.contains('UNKNOWN_RULE')
    }

    def 'missing application maps to MojoExecutionException'() {
        given:
        set('appDir', new File(directory, 'absent'))
        when:
        mojo.execute()
        then:
        thrown(MojoExecutionException)
    }

    def 'rule execution failure maps to MojoExecutionException even report only'() {
        given:
        config.text = '''
            def configuration = mule_linter { rules { README } }
            configuration.rulesDsl.ruleSet.rules[0].metaClass.execute = { com.avioconsulting.mule.linter.model.Application app -> throw new IllegalStateException('rule failed') }
            configuration
        '''
        when:
        mojo.execute()
        then:
        def error = thrown(MojoExecutionException)
        error.message.contains('rule failed')
    }

    def 'report errors always map to MojoExecutionException including when findings enforcement enabled'() {
        given:
        mojo.outputDirectory = new File(directory, 'not-a-directory')
        mojo.outputDirectory.text = 'file'
        set('failBuild', enforce)
        when:
        mojo.execute()
        then:
        thrown(MojoExecutionException)
        where:
        enforce << [false, true]
    }

    def 'Maven logging and report contents are independent of formatter order'() {
        when:
        mojo.execute()
        def firstLog = new ArrayList<>(lines)
        def firstJson = new File(mojo.outputDirectory, 'mule-linter-report.json').text
        lines.clear()
        set('formats', [FormatOptionsEnum.JSON, FormatOptionsEnum.CONSOLE])
        mojo.execute()
        then:
        firstJson == new File(mojo.outputDirectory, 'mule-linter-report.json').text
        firstLog.findAll { !it.contains('Report formatter found') && !it.contains('report saved') } ==
                lines.findAll { !it.contains('Report formatter found') && !it.contains('report saved') }
    }

    def 'incomplete analysis warns and strict analysis maps to MojoExecutionException'() {
        given:
        config.text = '''
            def configuration = mule_linter { rules { README } }
            configuration.rulesDsl.ruleSet.rules[0].metaClass.execute = { com.avioconsulting.mule.linter.model.Application app ->
                app.analysisWarnings.add('Unresolved parent fixture')
                []
            }
            configuration
        '''
        when:
        mojo.execute()
        then:
        lines.contains('Analysis incomplete.')
        lines.any { it.contains('Analysis warning: Unresolved parent fixture') }

        when:
        set('strictAnalysis', true)
        mojo.execute()
        then:
        thrown(MojoExecutionException)
    }

    private void set(String name, Object value) {
        def field = MuleLinterValidateMojo.getDeclaredField(name)
        field.accessible = true
        field.set(mojo, value)
    }
}
