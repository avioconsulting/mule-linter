package com.avioconsulting.mule.maven.mojo

import groovy.xml.XmlSlurper
import spock.lang.Specification

import java.util.zip.ZipFile

class PluginPackagingTest extends Specification {
    def 'published plugin preserves its descriptor and isolates Resolver 2 from Maven'() {
        given:
        def jar = new ZipFile(System.getProperty('mule.linter.pluginJar'))

        when:
        def entries = Collections.list(jar.entries())*.name
        def descriptor = new XmlSlurper().parse(jar.getInputStream(jar.getEntry('META-INF/maven/plugin.xml')))
        def providers = jar.getInputStream(jar.getEntry('META-INF/services/com.avioconsulting.mule.linter.spi.RuleProvider')).getText('UTF-8')

        then:
        entries.size() == entries.toSet().size()
        descriptor.groupId.text() == 'com.avioconsulting.mule'
        descriptor.artifactId.text() == 'mule-linter-maven-plugin'
        descriptor.mojos.mojo.goal*.text().contains('validate')
        entries.contains('com/avioconsulting/mule/internal/aether/RepositorySystemSession$CloseableSession.class')
        entries.contains('com/avioconsulting/mule/internal/aether/supplier/RepositorySystemSupplier.class')
        entries.contains('com/avioconsulting/mule/internal/maven/settings/Settings.class')
        !entries.any { it.startsWith('org/eclipse/aether/') }
        providers.contains('com.avioconsulting.mule.linter.catalog.BuiltinRuleProvider')

        cleanup:
        jar.close()
    }
}
