package com.avioconsulting.mule.linter.dsl

import org.codehaus.groovy.control.CompilerConfiguration

/** Configuration-only entrypoint: never constructs an application or resolves parent POMs. */
final class ConfigurationLoader {
    static MuleLinterDsl load(File file, PrintWriter diagnostics = new PrintWriter(System.err, true)) {
        if (file == null || !file.isFile()) throw new IllegalArgumentException("Configuration file does not exist: $file")
        String name = file.name.toLowerCase(Locale.ROOT)
        if (name.endsWith('.yaml') || name.endsWith('.yml')) return YamlConfigurationLoader.load(file)
        if (!name.endsWith('.groovy')) throw new IllegalArgumentException('Configuration must use .yaml, .yml, or .groovy')
        def compiler = new CompilerConfiguration(scriptBaseClass: Dsl.name)
        def binding = new Binding([params: [:], out: diagnostics])
        def evaluated = new GroovyShell(ConfigurationLoader.classLoader, binding, compiler).evaluate(file)
        if (!(evaluated instanceof MuleLinterDsl) || evaluated.rulesDsl == null) {
            throw new IllegalArgumentException('Configuration must return mule_linter { rules { ... } }')
        }
        evaluated as MuleLinterDsl
    }
}
