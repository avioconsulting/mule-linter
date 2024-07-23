package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import com.avioconsulting.mule.linter.rule.FileExistsRule
import org.yaml.snakeyaml.Yaml
/**
 * This rule checks that a azure-pipelines.yml file exists.
 */
class AzurePipelinesExistsRule extends FileExistsRule {

    static final String RULE_ID = 'AZURE_PIPELINES_EXISTS'
    static final String RULE_NAME = 'A azure-pipelines.yml file exists. '
    static final String RULE_VIOLATION_MESSAGE = 'azure-pipelines.yml file does not exist'
    static final String RULE_FORMAT_VIOLATION_MESSAGE = 'azure-pipelines.yml file is not in the correct YAML format'
    public static final String AZURE_PIPELINES = 'azure-pipelines.yml'

    AzurePipelinesExistsRule() {
        super(RULE_ID, RULE_NAME, AZURE_PIPELINES, RULE_VIOLATION_MESSAGE)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = super.execute(app)

        File azurePipelinesFile = getFile(app.applicationPath)

        if (violations.empty)  {
            def yaml = new Yaml()
            try {
                yaml.load(new FileInputStream(azurePipelinesFile))
            }
            catch (e) {
                def message = RULE_FORMAT_VIOLATION_MESSAGE + ': ' + e.message
                violations.add(new RuleViolation(this,
                                                    azurePipelinesFile.toString(),
                                                 0,
                                                 message))
            }
        }

        return violations
    }
}
