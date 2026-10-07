package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class GlobalFilesNoFlowsRule extends Rule {
    static final String RULE_ID = 'GLOBAL_FILES_NO_FLOWS'

    /** Full-match regexes against paths relative to src/main/mule. */
    @Param('patterns') List<String> patterns = ['global/.*\\.xml', 'global\\.xml', 'global-config\\.xml']
    /** Exact paths; these do not exempt GLOBAL_CONFIG_SEPARATION. */
    @Param('exceptions') List<String> exceptions = []

    GlobalFilesNoFlowsRule() {
        super(RULE_ID, 'Global files contain no flows or subflows except declared shared behavior')
    }

    @Override
    void init() {
        patterns.each { java.util.regex.Pattern.compile(it) }
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []
        app.configurationFiles.each { file ->
            String path = GlobalConfigSeparationRule.relativePath(app, file.file)
            if (!exceptions.contains(path) && patterns.any { path.matches(it) }) {
                file.allFlows.each { flow ->
                    violations.add(new RuleViolation(this, file.path, flow.lineNumber,
                        'Flow or subflow is in a global file: ' + flow.name))
                }
            }
        }
        violations
    }
}
