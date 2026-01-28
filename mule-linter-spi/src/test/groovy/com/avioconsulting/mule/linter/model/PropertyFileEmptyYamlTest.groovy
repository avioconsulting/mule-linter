package com.avioconsulting.mule.linter.model

import spock.lang.Specification

class PropertyFileEmptyYamlTest extends Specification {

    private File appDir
    private File yamlFile

    def setup() {
        appDir = File.createTempDir()
        yamlFile = new File(appDir, 'empty.yaml')
    }

    def cleanup() {
        yamlFile.delete()
        appDir.deleteDir()
    }

    def "empty yaml file does not throw"() {
        given:
        yamlFile.text = ''

        when:
        PropertyFile pf = new PropertyFile(yamlFile)

        then:
        noExceptionThrown()
        pf.getProperties() != null
        pf.getProperties().size() == 0
    }

    def "whitespace-only yaml file does not throw"() {
        given:
        // Whitespace-only YAML should be treated as empty and not throw.
        yamlFile.text = '\n  \n  \n'

        when:
        PropertyFile pf = new PropertyFile(yamlFile)

        then:
        noExceptionThrown()
        pf.getProperties() != null
        pf.getProperties().size() == 0
    }

    def "comment-only yaml file does not throw"() {
        given:
        yamlFile.text = '# placeholder\n'

        when:
        PropertyFile pf = new PropertyFile(yamlFile)

        then:
        noExceptionThrown()
        pf.getProperties() != null
        pf.getProperties().size() == 0
    }
}
