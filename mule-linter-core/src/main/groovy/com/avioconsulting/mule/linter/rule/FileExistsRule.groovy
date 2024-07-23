package com.avioconsulting.mule.linter.rule

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

class FileExistsRule extends Rule {
    static final String RULE_ID = 'FILE_EXISTS'
    static final String RULE_NAME = 'A File exists. '
    static final String RULE_VIOLATION_MESSAGE = 'A %s file does not exist'

    private String violationMessage;

    FileExistsRule(){
        super(RULE_ID, RULE_NAME)
    }

    protected FileExistsRule(String ruleId, String ruleName, String path) {
        this(ruleId, ruleName, path , null)
    }

    protected FileExistsRule(String ruleId, String ruleName, String path, String violationMessage) {
        super(ruleId, ruleName)
        this.path = path
        this.violationMessage = violationMessage;
    }
    /**
     * path of the file to check existence. It must be relative to application base folder.
     * For example, `Jenkinsfile`, `docs/index.md`.
     */
    @Param("path") String path

    File getFile(File applicationPath) {
        return new File(applicationPath, path);
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        boolean exists = app.hasFile(path)
        if (!exists) {
            violations.add(new RuleViolation(this, path, 0, violationMessage != null ? violationMessage :
                    String.format(RULE_VIOLATION_MESSAGE,path)))
        }

        return violations
    }

}
