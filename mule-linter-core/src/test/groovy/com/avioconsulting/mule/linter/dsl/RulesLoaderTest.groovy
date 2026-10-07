package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.rule.configuration.LoggerAttributesRule
import spock.lang.Specification

class RulesLoaderTest extends Specification {
    def "discovery preserves rule identities and GDSL exposes runtime options"() {
        when:
        def discovered = RulesLoader.rulesMap
        def generated = new GDSLGenerator().getRulesMap(null)

        then:
        discovered['LOGGER_REQUIRED_ATTRIBUTES'] == LoggerAttributesRule
        discovered.every { id, ruleClass -> ruleClass.newInstance().ruleId == id }
        generated.keySet() == discovered.keySet()
        generated.every { id, meta -> meta.ruleId == id && meta.ruleClass == discovered[id] }
        generated['LOGGER_REQUIRED_ATTRIBUTES'].params*.first.containsAll(['requiredAttributes', 'severity', 'ruleType', 'ruleName'])
        generated['LOGGER_REQUIRED_ATTRIBUTES'].params.find { it.first == 'requiredAttributes' }.second == 'java.util.List<java.lang.String>'
    }

    def "duplicate rule IDs fail with both class names independent of discovery order"() {
        given:
        def loader = new GroovyClassLoader(this.class.classLoader)
        Class first = fixture(loader, 'FirstRule')
        Class second = fixture(loader, 'SecondRule')

        when:
        RulesLoader.indexRules(reverse ? [second, first] : [first, second])

        then:
        def error = thrown(IllegalArgumentException)
        error.message == "Duplicate RULE_ID 'DUPLICATE_TEST' declared by FirstRule and SecondRule"

        cleanup:
        loader.close()

        where:
        reverse << [false, true]
    }

    def "inherited annotated fields remain configurable and internal fields stay hidden"() {
        given:
        def loader = new GroovyClassLoader(this.class.classLoader)
        Class ruleClass = loader.parseClass('''
            import com.avioconsulting.mule.linter.model.rule.*
            import com.avioconsulting.mule.linter.model.Application
            class ParentOptionRule extends Rule {
                @Param('paths') List<String> paths
                Object internalState
                List<RuleViolation> execute(Application app) { [] }
            }
            class ChildOptionRule extends ParentOptionRule {}
        ''')
        Class child = loader.loadClass('ChildOptionRule')
        Rule rule = child.newInstance()
        def options = new RuleOptions(rule)

        when:
        options.paths = ['one']

        then:
        rule.paths == ['one']
        RuleOptions.fieldsFor(child).keySet() == ['paths', 'severity', 'ruleType', 'ruleName'] as Set

        cleanup:
        loader.close()
    }

    private Class fixture(GroovyClassLoader loader, String name) {
        loader.parseClass("""
            import com.avioconsulting.mule.linter.model.rule.*
            import com.avioconsulting.mule.linter.model.Application
            class ${name} extends Rule {
                static final String RULE_ID = 'DUPLICATE_TEST'
                List<RuleViolation> execute(Application app) { [] }
            }
        """)
    }
}
