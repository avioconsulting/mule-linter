package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/** Global configuration may span files, but must stay separate from processing. */
class GlobalConfigSeparationRule extends Rule {
    static final String RULE_ID = 'GLOBAL_CONFIG_SEPARATION'

    /** Exact paths relative to src/main/mule, using forward slashes. */
    @Param('exceptions') List<String> exceptions = []

    GlobalConfigSeparationRule() {
        super(RULE_ID, 'Global configuration is separate from flows and subflows')
    }

    static String relativePath(Application app, File file) {
        new File(app.applicationPath, app.CONFIGURATION_PATH).toPath().toAbsolutePath().normalize()
            .relativize(file.toPath().toAbsolutePath().normalize()).toString().replace('\\', '/')
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        app.configurationFiles.each { file ->
            if (!exceptions.contains(relativePath(app, file.file)) && !file.allFlows.empty) {
                file.findGlobalConfigs().each { config ->
                    violations.add(new RuleViolation(this, file.path, config.lineNumber,
                        'Global configuration shares a file with flows or subflows: ' + config.componentName))
                }
            }
        }
        violations
    }
}
