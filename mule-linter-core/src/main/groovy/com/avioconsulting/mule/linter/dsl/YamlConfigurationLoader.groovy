package com.avioconsulting.mule.linter.dsl

import com.avioconsulting.mule.linter.catalog.RuleCatalog
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.YAMLException

/** Data-only YAML loading; configuration shares catalog and domain validation with Groovy. */
final class YamlConfigurationLoader {
    static MuleLinterDsl load(File file) {
        def options = new LoaderOptions()
        options.allowDuplicateKeys = false
        options.allowRecursiveKeys = false
        options.maxAliasesForCollections = 0
        options.nestingDepthLimit = 50
        options.codePointLimit = 1024 * 1024
        Object document
        try {
            file.withReader('UTF-8') { reader -> document = new Yaml(new SafeConstructor(options)).load(reader) }
        } catch (YAMLException e) {
            throw new IllegalArgumentException("Invalid YAML configuration '${file.name}': ${e.message}", e)
        }
        requireMap(document, 'configuration')
        rejectUnknown(document as Map, ['schemaVersion', 'rules'], 'configuration')
        if (!(document.schemaVersion instanceof Integer) || document.schemaVersion != 1) {
            throw new IllegalArgumentException('YAML configuration requires schemaVersion: 1')
        }
        if (!(document.rules instanceof List)) throw new IllegalArgumentException('YAML configuration requires a rules list')

        def configuration = new MuleLinterDsl(rulesDsl: new RulesDsl())
        document.rules.eachWithIndex { entry, index ->
            String location = "rules[$index]"
            requireMap(entry, location)
            rejectUnknown(entry as Map, ['rule', 'options'], location)
            if (!(entry.rule instanceof String) || !entry.rule.trim()) {
                throw new IllegalArgumentException("$location requires a string rule identifier")
            }
            Map supplied = [:]
            if (entry.containsKey('options')) {
                requireMap(entry.options, "${location}.options")
                supplied = entry.options as Map
            }
            try {
                def definition = RuleCatalog.instance.resolve(entry.rule)
                configuration.rulesDsl.addSpecification(definition.specification(supplied))
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("$location: ${e.message}", e)
            }
        }
        configuration
    }

    private static void requireMap(Object value, String location) {
        if (!(value instanceof Map)) throw new IllegalArgumentException("$location must be a mapping")
    }

    private static void rejectUnknown(Map values, List<String> allowed, String location) {
        values.keySet().each { key ->
            if (!(key instanceof String) || !allowed.contains(key)) throw new IllegalArgumentException("Unknown key '$key' in $location; expected $allowed")
        }
    }
}
