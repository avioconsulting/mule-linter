package com.avioconsulting.mule.linter.rule.cicd

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.AzurePipelinesFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleSeverity
import com.avioconsulting.mule.linter.model.rule.RuleType
import com.avioconsulting.mule.linter.model.rule.RuleViolation
import org.yaml.snakeyaml.Yaml
/**
 * This rule checks that a azure-pipelines.yml file exists.
 */
class AzurePipelinesExistsRule extends Rule{

    static final String RULE_ID = 'AZURE_PIPELINES_EXISTS'
    static final String RULE_NAME = 'A azure-pipelines.yml file exists. '
    static final String RULE_VIOLATION_MESSAGE = 'azure-pipelines.yml file does not exist'
    static final String RULE_FORMAT_VIOLATION_MESSAGE = 'azure-pipelines.yml file is not in the correct YAML format'

    AzurePipelinesExistsRule() {
        super(RULE_ID, RULE_NAME, RuleSeverity.CRITICAL, RuleType.CODE_SMELL)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        AzurePipelinesFile azurePipelinesFile = app.azurePipelinesFile
        if (azurePipelinesFile == null || !azurePipelinesFile.doesExist()) {
            violations.add(new RuleViolation(this, app.azurePipelinesFile.file.toString(), 0, RULE_VIOLATION_MESSAGE))
        } else {
            def yaml = new Yaml()
            try {
                yaml.load(new FileInputStream(azurePipelinesFile.file))
            }
            catch (e) {
                def message = RULE_FORMAT_VIOLATION_MESSAGE + ': ' + e.message
                violations.add(new RuleViolation(this,
                                                 app.azurePipelinesFile.file.toString(),
                                                 0,
                                                 message))
            }
        }

        return violations
    }
}
