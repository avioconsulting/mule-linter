package com.avioconsulting.mule.linter.dsl

import org.codehaus.groovy.control.CompilerConfiguration
import spock.lang.Specification

class MuleLinterDslTest extends Specification {

    def "Test Rule DSL"() {
        given:
        def dsl = this.class.getClassLoader().getResource("TestRules.groovy").path

        when:
        def compilerConfig = new CompilerConfiguration().with {
            scriptBaseClass = Dsl.name
            it
        }
        def binding = new Binding()
        binding.setVariable('params',[:])

        def shell = new GroovyShell(
                this.class.classLoader,
                binding,
                compilerConfig
        )
        MuleLinterDsl ruleConfig = shell.evaluate(new File(dsl)) as MuleLinterDsl
        then:
        true


    }

}
